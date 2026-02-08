package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class Card {

    private final String name;
    private final String type;
    private final String subtype;
    private final String manaProduced;
    private final List<CardEffect> onTapEffects;
}
