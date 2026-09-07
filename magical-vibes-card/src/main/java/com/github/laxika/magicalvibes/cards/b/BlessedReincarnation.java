package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "47")
public class BlessedReincarnation extends Card {

    public BlessedReincarnation() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL,
                        new ExileTargetThenRevealUntilTypeToBattlefieldEffect(Set.of(CardType.CREATURE)));
    }
}
