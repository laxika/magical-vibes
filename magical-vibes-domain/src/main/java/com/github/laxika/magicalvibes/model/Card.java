package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class Card {

    private final String name;
    private final CardType type;
    private final List<CardSubtype> subtypes;
    private final String manaProduced;
    private final List<CardEffect> onTapEffects;
    private final String manaCost;
    private final Integer power;
    private final Integer toughness;
    private final Set<Keyword> keywords;
    private final List<CardEffect> onEnterBattlefieldEffects;
    private final List<CardEffect> spellEffects;
}
