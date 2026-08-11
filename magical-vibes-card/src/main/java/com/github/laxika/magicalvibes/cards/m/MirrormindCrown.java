package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.MirrormindCrownEffect;

@CardRegistration(set = "ECL", collectorNumber = "258")
public class MirrormindCrown extends Card {

    public MirrormindCrown() {
        addEffect(EffectSlot.STATIC, new MirrormindCrownEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
