package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class SummonAlexander extends Card {

    public SummonAlexander() {
        var creaturesYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));

        addTrigger(Triggers.chapter(1),
                PreventDamageEffect.allToControlledMatchingPermanents(creaturesYouControl));
        addTrigger(Triggers.chapter(2),
                PreventDamageEffect.allToControlledMatchingPermanents(creaturesYouControl));
        addTrigger(Triggers.chapter(3), new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
