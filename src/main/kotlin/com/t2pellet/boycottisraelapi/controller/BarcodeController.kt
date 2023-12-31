package com.t2pellet.boycottisraelapi.controller

import com.t2pellet.boycottisraelapi.model.*
import com.t2pellet.boycottisraelapi.service.BarcodeService
import com.t2pellet.boycottisraelapi.service.BoycottService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/api/barcode")
class BarcodeController(
    val barcodeService: BarcodeService,
    val boycottService: BoycottService
) {

    @GetMapping("/{barcode}")
    fun getBarcode(@PathVariable barcode: String): BoycottBarcode {
        val barcodeData = barcodeService.getBarcodeEntry(barcode)
        if (barcodeData != null) {
            val isFromCache = barcodeService.isCachedBarcode(barcodeData.barcode)
            if (barcodeData.strapiId != null) {
                val parent = boycottService.get(barcodeData.strapiId)
                val result = BoycottBarcode(barcodeData, parent)
                return result
            } else if (!isFromCache) {
                val result = boycottService.getForBarcode(barcodeData)
                barcodeService.saveBarcode(barcodeData.barcode, result)
                return result
            } else {
                val logo = if (barcodeData.company.isNotEmpty()) boycottService.getLogo(barcodeData.company) else null
                return BoycottBarcode(barcodeData.product, barcodeData.company, false, null, logo)
            }
        }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Barcode not found")
    }

    @GetMapping("/exists/{barcode}")
    fun checkBarcode(@PathVariable barcode: String): BarcodeCheck {
        val cached = barcodeService.isCachedBarcode(barcode)
        return BarcodeCheck(barcode, cached)
    }

    @PostMapping("")
    fun addBarcode(@RequestBody barcode: BarcodeData): BarcodeEntry {
        if (barcodeService.isCachedBarcode(barcode.barcode)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot add an existing entry")
        }
        val match: BoycottEntry? = boycottService.getBest(barcode.company.ifEmpty { barcode.product })
        if (match != null) {
            val entry = BarcodeEntry(barcode.barcode, barcode.company, barcode.product, match.id)
            barcodeService.saveBarcode(entry)
            return entry
        } else {
            val entry = BarcodeEntry(barcode.barcode, barcode.company, barcode.product)
            barcodeService.saveBarcode(entry)
            return entry
        }
    }

    @PatchMapping("/{barcode}")
    fun fixBarcode(@PathVariable barcode: String, @RequestParam company: String): BarcodeEntry {
        val match: BoycottEntry? = boycottService.getBest(company)
        if (match != null) {
            val result = barcodeService.saveBarcodeCompany(barcode, match.name, match.id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Barcode not found")
            return result
        }
        val result = barcodeService.saveBarcodeCompany(barcode, company)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Barcode not found")
        return result
    }
}