package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "124")
public class OrzhovPontiff extends Card {

    public OrzhovPontiff() {
        ChooseOneEffect modes = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+1 until end of turn",
                        new BoostAllOwnCreaturesEffect(1, 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you don't control get -1/-1 until end of turn",
                        new BoostAllCreaturesEffect(-1, -1,
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))
        ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(modes));
        addEffect(EffectSlot.ON_HAUNTED_CREATURE_DIES, new ChooseOneAtTriggerTimeEffect(modes));
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_DEATH, new HauntEffect());
    }
}
