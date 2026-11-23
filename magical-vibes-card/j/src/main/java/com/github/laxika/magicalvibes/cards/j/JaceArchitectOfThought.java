package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;
import com.github.laxika.magicalvibes.model.effect.SearchEachPlayerLibraryForNonlandCardToExileAndCastEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "44")
@CardRegistration(set = "DDM", collectorNumber = "1")
@CardRegistration(set = "PIO", collectorNumber = "329")
@CardRegistration(set = "C20", collectorNumber = "114")
@CardRegistration(set = "NEC", collectorNumber = "93")
public class JaceArchitectOfThought extends Card {

    public JaceArchitectOfThought() {
        // +1: Until your next turn, whenever a creature an opponent controls attacks, it gets -1/-0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(RegisterGlobalTriggeredAbilityEffect.untilYourNextTurn(new TriggeredAbility(
                        Triggers.creatureAttacks().withFilter(
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())),
                        new BoostTargetCreatureEffect(new Fixed(-1), new Fixed(0))))),
                "+1: Until your next turn, whenever a creature an opponent controls attacks, it gets -1/-0 until end of turn."
        ));

        // −2: Reveal the top three cards of your library. An opponent separates those cards into two
        // piles. Put one pile into your hand and the other on the bottom of your library in any order.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new RevealTopCardsAndSeparateEffect(3, CardPileDisposition.HAND_AND_BOTTOM)),
                "−2: Reveal the top three cards of your library. An opponent separates those cards into two piles. "
                        + "Put one pile into your hand and the other on the bottom of your library in any order."
        ));

        // −8: For each player, search that player's library for a nonland card and exile it, then that
        // player shuffles. You may cast those cards without paying their mana costs.
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new SearchEachPlayerLibraryForNonlandCardToExileAndCastEffect()),
                "−8: For each player, search that player's library for a nonland card and exile it, then that player "
                        + "shuffles. You may cast those cards without paying their mana costs."
        ));
    }
}
