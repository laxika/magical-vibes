package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

/**
 * Struggle // Survive — front half (Struggle).
 * Instant — Struggle deals damage to target creature equal to the number of lands you control.
 * Back half (Survive) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "151")
public class StruggleSurvive extends Card {

    public StruggleSurvive() {
        Survive survive = new Survive();
        survive.setSetCode(getSetCode());
        survive.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(survive);

        // Struggle deals damage to target creature equal to the number of lands you control.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }

    @Override
    public String getBackFaceClassName() {
        return "Survive";
    }
}
