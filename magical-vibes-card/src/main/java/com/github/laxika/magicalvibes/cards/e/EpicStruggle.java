package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "JUD", collectorNumber = "112")
public class EpicStruggle extends Card {

    public EpicStruggle() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new ControlsPermanentCount(20, new PermanentIsCreaturePredicate()),
                        new WinGameEffect()));
    }
}
