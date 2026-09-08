package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "167")
public class Transmogrify extends Card {

    public Transmogrify() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new ExileTargetThenRevealUntilTypeToBattlefieldEffect(Set.of(CardType.CREATURE)));
    }
}
