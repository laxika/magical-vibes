package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoalstokeGearhulk.class, GrizzlyBears.class, HolyDay.class, SerraAngel.class})
class CoalstokeGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a creature card with mana value 4 or less from any graveyard")
    void etbOffersMatchingCreatureFromAnyGraveyard() {
        Card ownEligible = new GrizzlyBears();
        Card opponentEligible = new GrizzlyBears();
        Card tooExpensive = new SerraAngel();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(ownEligible, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentEligible, nonCreature));

        castGearhulk();

        List<UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).containsExactly(ownEligible.getId(), opponentEligible.getId());
    }

    @Test
    @DisplayName("Returned creature gains the listed keywords and a finality counter")
    void returnedCreatureGetsKeywordsAndFinalityCounter() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        castGearhulk();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(returned.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(returned.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The returned creature waits for its controller's next end step before exile")
    void returnedCreatureExilesAtControllersNextEndStep() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        castGearhulk();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    private void castGearhulk() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CoalstokeGearhulk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exile at the next end step uses the stack and allows responses")
    void delayedExileAllowsResponses() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        castGearhulk();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("A target removed from its graveyard before resolution is not returned")
    void missingTargetIsNotReturned() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        castGearhulk();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Reanimation during an end step waits until the next matching end step")
    void resolvingDuringEndStepWaitsForNextEndStep() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        castGearhulk();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
