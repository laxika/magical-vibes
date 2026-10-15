package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "56")
public class GiantSizedFlyingAnt extends Card {

    public GiantSizedFlyingAnt() {
        var nonlandPermanentTarget = TargetFilters.nonlandPermanent();
        var nonlandPermanentPredicate = nonlandPermanentTarget.predicate();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Tap target nonland permanent.",
                        new TapPermanentsEffect(TapUntapScope.TARGET, nonlandPermanentPredicate),
                        nonlandPermanentTarget),
                new ChooseOneEffect.ChooseOneOption(
                        "Untap target nonland permanent.",
                        new UntapPermanentsEffect(TapUntapScope.TARGET, nonlandPermanentPredicate),
                        nonlandPermanentTarget)
        ))));
    }
}
