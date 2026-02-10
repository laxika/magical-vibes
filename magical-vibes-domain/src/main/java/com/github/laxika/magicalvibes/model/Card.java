package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class Card {

    private final String name;
    private final CardType type;
    private final String manaCost;

    @Setter private List<CardSubtype> subtypes = List.of();
    @Setter private String cardText;
    @Setter private List<CardEffect> onTapEffects = List.of();
    @Setter private Integer power;
    @Setter private Integer toughness;
    @Setter private Set<Keyword> keywords = Set.of();
    @Setter private List<CardEffect> onEnterBattlefieldEffects = List.of();
    @Setter private List<CardEffect> spellEffects = List.of();
    @Setter private String setCode;
    @Setter private String collectorNumber;
}
