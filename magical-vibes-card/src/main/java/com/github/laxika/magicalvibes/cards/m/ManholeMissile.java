package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutCardFromHandOnBottomOfLibraryThenDrawEffect;

@CardRegistration(set = "TMT", collectorNumber = "94")
public class ManholeMissile extends Card {

    public ManholeMissile() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));
        addEffect(EffectSlot.SPELL, new MayPutCardFromHandOnBottomOfLibraryThenDrawEffect());
    }
}
