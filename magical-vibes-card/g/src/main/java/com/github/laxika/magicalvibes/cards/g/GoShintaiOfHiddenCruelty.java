package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostControlledSubtypeCountPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "97")
public class GoShintaiOfHiddenCruelty extends Card {

    public GoShintaiOfHiddenCruelty() {
        PermanentAllOfPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentToughnessAtMostControlledSubtypeCountPredicate(CardSubtype.SHRINE)
        ));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayPayManaEffect("{1}",
                        new QueueReflexiveAbilityEffect(new DestroyTargetPermanentEffect(targetPredicate)),
                        "Pay {1} to destroy target creature with toughness X or less?"));
    }
}
