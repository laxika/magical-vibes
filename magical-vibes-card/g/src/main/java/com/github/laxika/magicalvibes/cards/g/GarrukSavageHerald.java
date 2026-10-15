package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "336")
public class GarrukSavageHerald extends Card {

    public GarrukSavageHerald() {
        // +1: Reveal the top card of your library. If it's a creature card, put it into your hand.
        // Otherwise, put it on the bottom of your library.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new LookAtTopCardsEffect(
                        new Fixed(1), new Fixed(1), new CardTypePredicate(CardType.CREATURE),
                        LookDestination.BOTTOM_OF_LIBRARY, true,
                        LibrarySearchDestination.HAND, false)),
                "+1: Reveal the top card of your library. If it's a creature card, put it into your hand. Otherwise, put it on the bottom of your library."
        ));

        // −2: Target creature you control deals damage equal to its power to another target creature.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new TargetDealsPowerDamageToTargetEffect()),
                "−2: Target creature you control deals damage equal to its power to another target creature.",
                null, -2, null, null,
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creature()), 2, 2
        ));

        // −7: Until end of turn, creatures you control gain "You may have this creature assign its
        // combat damage as though it weren't blocked."
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect(
                        new AssignCombatDamageAsThoughUnblockedEffect())),
                "−7: Until end of turn, creatures you control gain \"You may have this creature assign its combat damage as though it weren't blocked.\""
        ));
    }
}
