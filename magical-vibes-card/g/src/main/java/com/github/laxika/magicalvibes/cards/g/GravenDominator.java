package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "7")
public class GravenDominator extends Card {

    public GravenDominator() {
        SetBasePowerToughnessEffect setOtherCreaturesToOneOne =
                new SetBasePowerToughnessEffect(1, 1, GrantScope.ALL_CREATURES);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, setOtherCreaturesToOneOne);
        addTrigger(Triggers.dies(ObjectRelation.HAUNTED), setOtherCreaturesToOneOne);

        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.THIS), new HauntEffect());
    }
}
