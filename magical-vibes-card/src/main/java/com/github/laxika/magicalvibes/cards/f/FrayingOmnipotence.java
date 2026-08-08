package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsFractionOfHandRoundedUpEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesFractionRoundedUpEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "M19", collectorNumber = "97")
public class FrayingOmnipotence extends Card {

    public FrayingOmnipotence() {
        // Each step is recomputed per player against their own totals, rounded up each time.
        addEffect(EffectSlot.SPELL, new EachPlayerLosesFractionOfLifeRoundedUpEffect(2));
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsFractionOfHandRoundedUpEffect(2));
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesFractionRoundedUpEffect(2, new PermanentIsCreaturePredicate()));
    }
}
