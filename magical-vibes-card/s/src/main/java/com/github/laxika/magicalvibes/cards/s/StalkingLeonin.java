package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentForTargetingRelayEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealChosenPlayerCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceChosenPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "105")
@CardRegistration(set = "MKC", collectorNumber = "86")
@CardRegistration(set = "C17", collectorNumber = "7")
public class StalkingLeonin extends Card {

    public StalkingLeonin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentForTargetingRelayEffect(true));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new RevealChosenPlayerCost(),
                        new ConditionalEffect(
                                new TargetPermanentMatches(new PermanentControlledBySourceChosenPlayerPredicate()),
                                new ExileTargetPermanentEffect())),
                "Reveal the player you chose: Exile target creature that's attacking you if it's controlled by the chosen player. Activate only once.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsAttackingSourceControllerPredicate())),
                        "Target must be a creature attacking you"))
                .withMaxActivationsPerGame(1));
    }
}
