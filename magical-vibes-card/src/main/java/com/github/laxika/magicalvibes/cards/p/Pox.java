package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsFractionOfHandRoundedUpEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesFractionRoundedUpEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "5ED", collectorNumber = "189")
@CardRegistration(set = "ICE", collectorNumber = "158")
public class Pox extends Card {

    public Pox() {
        // Each player, in order, loses / discards / sacrifices a third of the relevant quantity,
        // rounded up each time. Each step is recomputed per player against their own totals.
        addEffect(EffectSlot.SPELL, new EachPlayerLosesFractionOfLifeRoundedUpEffect(3));
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsFractionOfHandRoundedUpEffect(3));
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesFractionRoundedUpEffect(3, new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesFractionRoundedUpEffect(3, new PermanentIsLandPredicate()));
    }
}
