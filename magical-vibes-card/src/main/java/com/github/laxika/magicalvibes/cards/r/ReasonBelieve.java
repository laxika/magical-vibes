package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.Believe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

/**
 * Reason // Believe — front half (Reason).
 * Sorcery — Scry 3.
 * Back half (Believe) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "154")
public class ReasonBelieve extends Card {

    public ReasonBelieve() {
        Believe believe = new Believe();
        believe.setSetCode(getSetCode());
        believe.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(believe);

        // Scry 3.
        addEffect(EffectSlot.SPELL, new ScryEffect(3));
    }

    @Override
    public String getBackFaceClassName() {
        return "Believe";
    }
}
