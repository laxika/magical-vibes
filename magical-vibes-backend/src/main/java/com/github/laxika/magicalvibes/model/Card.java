package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Card {

    private final String name;
    private final String type;
    private final String subtype;
    private final String manaProduced;
}
