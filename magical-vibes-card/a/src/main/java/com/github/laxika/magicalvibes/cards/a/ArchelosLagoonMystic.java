package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsEnterUntappedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "140")
public class ArchelosLagoonMystic extends Card {

    private static final Set<CardType> PERMANENT_TYPES = Set.of(
            CardType.LAND,
            CardType.CREATURE,
            CardType.ENCHANTMENT,
            CardType.ARTIFACT,
            CardType.PLANESWALKER,
            CardType.BATTLE,
            CardType.KINDRED
    );

    public ArchelosLagoonMystic() {
        addStaticEffect(new ConditionalReplacementEffect(
                new SourceIsTapped(), new EnterPermanentsOfTypesTappedEffect(PERMANENT_TYPES)));
        addStaticEffect(new ConditionalReplacementEffect(
                new SourceUntapped(), new AllPermanentsEnterUntappedEffect(new PermanentTruePredicate())));
    }
}
