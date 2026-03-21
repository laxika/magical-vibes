package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public record AnimateLandEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                Set<Keyword> grantedKeywords, CardColor animatedColor,
                                Set<CardType> grantedCardTypes, GrantScope scope,
                                EffectDuration duration) implements CardEffect {

    /** Self-targeting, until end of turn (manlands). */
    public AnimateLandEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                             Set<Keyword> grantedKeywords, CardColor animatedColor) {
        this(power, toughness, grantedSubtypes, grantedKeywords, animatedColor, Set.of(),
                GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN);
    }

    /** Self-targeting, until end of turn, with granted card types (e.g. Inkmoth Nexus). */
    public AnimateLandEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                             Set<Keyword> grantedKeywords, CardColor animatedColor,
                             Set<CardType> grantedCardTypes) {
        this(power, toughness, grantedSubtypes, grantedKeywords, animatedColor, grantedCardTypes,
                GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN);
    }

    @Override
    public boolean isSelfTargeting() { return scope == GrantScope.SELF; }
}
