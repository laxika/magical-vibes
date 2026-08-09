package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.Victory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Onward // Victory — front half (Onward).
 * Instant — Target creature gets +X/+0 until end of turn, where X is its power.
 * Back half (Victory) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "218")
public class OnwardVictory extends Card {

    public OnwardVictory() {
        setBackFaceCard(new Victory());

        // Target creature gets +X/+0 until end of turn, where X is its power.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new BoostTargetCreatureEffect(new TargetPower(), new Fixed(0)));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "Victory";
    }
}
