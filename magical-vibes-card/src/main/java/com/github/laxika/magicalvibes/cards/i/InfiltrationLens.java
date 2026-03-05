package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SOM", collectorNumber = "167")
public class InfiltrationLens extends Card {

    public InfiltrationLens() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new MayEffect(new DrawCardEffect(2), "Draw two cards?"), TriggerMode.PER_BLOCKER);
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
