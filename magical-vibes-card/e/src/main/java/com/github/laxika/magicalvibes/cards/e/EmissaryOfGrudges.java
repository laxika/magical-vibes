package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.RevealChosenPlayerCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByChosenPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;

import java.util.List;

@CardRegistration(set = "C18", collectorNumber = "20")
public class EmissaryOfGrudges extends Card {

    public EmissaryOfGrudges() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentOnEnterEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RevealChosenPlayerCost(),
                        new ChangeTargetOfTargetSpellWithSingleTargetEffect()
                ),
                "Reveal the player you chose: Choose new targets for target spell or ability if it's controlled by the chosen player and if it targets you or a permanent you control. Activate only once.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryIsSingleTargetPredicate(),
                                new StackEntryControlledByChosenPlayerPredicate(),
                                new StackEntryAnyOfPredicate(List.of(
                                        new StackEntryTargetsYouPredicate(),
                                        new StackEntryTargetsYourPermanentPredicate())
                        ))),
                        "Target must be a single-target spell or ability controlled by the chosen player that targets you or a permanent you control."
                )
        ).withMaxActivationsPerGame(1));
    }
}
