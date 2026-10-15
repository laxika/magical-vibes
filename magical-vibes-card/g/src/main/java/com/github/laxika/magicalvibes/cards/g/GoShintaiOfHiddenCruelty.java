package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostControlledSubtypeCountPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "97")
public class GoShintaiOfHiddenCruelty extends Card {

    public GoShintaiOfHiddenCruelty() {
        PermanentAllOfPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentToughnessAtMostControlledSubtypeCountPredicate(CardSubtype.SHRINE)
        ));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                MayPayManaEffect.reflexiveTarget("{1}", new DestroyTargetPermanentEffect(targetPredicate),
                        "Pay {1} to destroy target creature with toughness X or less?"));
    }
}
