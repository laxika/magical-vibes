package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/** Permanently animates the permanent recorded as the resolving entry's chosen permanent. */
public record AnimateChosenPermanentEffect(AnimatePermanentsEffect animation) implements CardEffect {

    public AnimateChosenPermanentEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                        Set<Keyword> grantedKeywords) {
        this(new AnimatePermanentsEffect(power, toughness, grantedSubtypes, grantedKeywords, null,
                Set.of(), GrantScope.TARGET, EffectDuration.PERMANENT));
    }
}
