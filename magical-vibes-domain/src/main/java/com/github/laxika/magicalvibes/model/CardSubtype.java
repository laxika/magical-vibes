package com.github.laxika.magicalvibes.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CardSubtype {

    FOREST("Forest"),
    BEAR("Bear"),
    ELF("Elf"),
    DRUID("Druid");

    @JsonValue
    @Getter
    private final String displayName;
}
