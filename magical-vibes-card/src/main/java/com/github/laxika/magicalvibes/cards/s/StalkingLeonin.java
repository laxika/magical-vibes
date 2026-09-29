package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealChosenPlayerCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceChosenPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "105")
@CardRegistration(set = "MKC", collectorNumber = "86")
public class StalkingLeonin extends Card {

    public StalkingLeonin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentOnEnterEffect());

        PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingSourceControllerPredicate(),
                new PermanentControlledBySourceChosenPlayerPredicate()));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new RevealChosenPlayerCost(), new ExileTargetPermanentEffect(targetPredicate)),
                "Reveal the player you chose: Exile target creature that's attacking you if it's controlled by the chosen player. Activate only once.",
                new PermanentPredicateTargetFilter(
                        targetPredicate,
                        "Target must be a creature attacking you controlled by the chosen player"))
                .withMaxActivationsPerGame(1));
    }
}
