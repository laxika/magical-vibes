package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RaiseDead;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Cheerful Osteomancer // Raise Dead (SOS 76).
 * <p>
 * Front face — 4/2 Orc Warlock with Prepared (auto-loaded from Scryfall keywords) and:
 * "This creature enters prepared." While prepared, a copy of its prepare spell {@link RaiseDead} sits in
 * exile and may be cast; casting it unprepares it.
 */
@CardRegistration(set = "SOS", collectorNumber = "76")
public class CheerfulOsteomancerRaiseDead extends Card {

    public CheerfulOsteomancerRaiseDead() {
        RaiseDead prepareSpell = new RaiseDead();
        prepareSpell.setSetCode(getSetCode());
        prepareSpell.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(prepareSpell);

        // This creature enters prepared.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "RaiseDead";
    }
}
