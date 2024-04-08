package com.example.etcd;

import io.micrometer.core.instrument.Meter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.instrument.Metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class MetricsController {


    @GetMapping("/metrics/list")
    public String listAllMetrics() {
        List<Meter.Id> metricIds = Metrics.globalRegistry.getMeters().stream()
                .map(Meter::getId)
                .toList();
        System.out.println(metricIds);
        return metricIds.toString();
    }
}
