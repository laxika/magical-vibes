package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M20", collectorNumber = "63")
public class HardCover extends Card {

    public HardCover() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(0, 2, Subjects.attachedCreature()))
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(new DrawCardEffect(1), new DiscardEffect(1, Subjects.you())),
                                "{T}: Draw a card, then discard a card."
                        ),
                        Subjects.attachedCreature()
                ));
    }
}
