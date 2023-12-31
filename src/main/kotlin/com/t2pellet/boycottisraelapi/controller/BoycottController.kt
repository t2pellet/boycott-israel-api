package com.t2pellet.boycottisraelapi.controller

import com.t2pellet.boycottisraelapi.model.BoycottEntry
import com.t2pellet.boycottisraelapi.service.BoycottService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/boycott")
class BoycottController(val boycottService: BoycottService) {

    @GetMapping("")
    fun get(@RequestParam name: Optional<String>): List<BoycottEntry> {
        if (name.isPresent) {
            return boycottService.get(name.get())
        }
        return boycottService.getAll()
    }

    @GetMapping("{id}")
    fun get(@PathVariable id: Number): BoycottEntry {
        return boycottService.get(id)
    }
}