package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NuisanceEngine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BennieBracksZoologist.class, NuisanceEngine.class})
class BennieBracksZoologistTest extends BaseCardTest {

    @Test
    void drawsAtEndStepAfterCreatingAToken() {
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        int handSizeBeforeEndStep = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeEndStep + 1);
    }

    @Test
    void doesNotDrawWithoutCreatingAToken() {
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist()));
        int handSizeBeforeEndStep = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeEndStep);
    }

    @Test
    void triggersAtEachPlayersEndStep() {
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        int handSizeBeforeEndStep = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeEndStep + 1);
    }

    @Test
    void opponentCreatingATokenDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.addToBattlefield(player2, new NuisanceEngine());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsOnlyOneCardAfterCreatingMultipleTokens() {
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist(), new BennieBracksZoologist()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void countsTokensCreatedBeforeBennieEnteredBattlefield() {
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void tokenCreatedAfterEndStepBeginsDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenCreatedOnPreviousTurnDoesNotQualify() {
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist(), new BennieBracksZoologist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void canConvokeWithNewlyCreatedColorlessToken() {
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        var pest = findPermanent(player1, "Pest");
        harness.setHand(player1, List.of(new BennieBracksZoologist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(pest.getId()));

        assertThat(pest.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bennie Bracks, Zoologist");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
