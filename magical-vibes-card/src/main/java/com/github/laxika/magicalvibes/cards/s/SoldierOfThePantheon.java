package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMulticoloredEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "32")
public class SoldierOfThePantheon extends Card {

    public SoldierOfThePantheon() {
        addEffect(EffectSlot.STATIC, new ProtectionFromMulticoloredEffect());

        // Whenever an opponent casts a multicolored spell, you gain 1 life.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardIsMulticoloredPredicate(), List.of(new GainLifeEffect(1))));
    }
}
