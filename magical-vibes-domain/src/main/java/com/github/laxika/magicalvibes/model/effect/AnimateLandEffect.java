package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public record AnimateLandEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                Set<Keyword> grantedKeywords, CardColor animatedColor,
                                Set<CardType> grantedCardTypes) implements CardEffect {

    public AnimateLandEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                             Set<Keyword> grantedKeywords, CardColor animatedColor) {
        this(power, toughness, grantedSubtypes, grantedKeywords, animatedColor, Set.of());
    }

    @Override
    public boolean isSelfTargeting() { return true; }
}
