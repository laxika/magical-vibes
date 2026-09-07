package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringSpellControllerEffect;

@CardRegistration(set = "ELD", collectorNumber = "115")
public class BonecrusherGiant extends Card {

    public BonecrusherGiant() {
        setBackFaceCard(new Stomp());
        addCastingOption(new AdventureCast("{1}{R}"));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL,
                new DealDamageToTriggeringSpellControllerEffect(2));
    }

    @Override
    public String getBackFaceClassName() {
        return "Stomp";
    }
}
