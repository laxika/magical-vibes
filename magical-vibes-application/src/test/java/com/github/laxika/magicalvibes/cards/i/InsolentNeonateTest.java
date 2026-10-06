package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsolentNeonate.class, Forest.class})
class InsolentNeonateTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and sacrificing Insolent Neonate draws a card")
    void discardsSacrificesAndDraws() {
        harness.addToBattlefield(player1, new InsolentNeonate());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Insolent Neonate");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Insolent Neonate cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new InsolentNeonate());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void costsArePaidBeforeDrawingAndOnlyControllerDraws() {
        harness.addToBattlefield(player1, new InsolentNeonate());
        harness.setHand(player1, List.of(new Forest(), new InsolentNeonate()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Insolent Neonate");
        harness.assertInGraveyard(player1, "Insolent Neonate");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInHand(player1, "Insolent Neonate");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void canActivateWhileTappedAndSummoningSickDuringOpponentsTurn() {
        Permanent neonate = harness.addToBattlefieldAndReturn(player1, new InsolentNeonate());
        neonate.setSummoningSick(true);
        neonate.tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Insolent Neonate");
        harness.assertInGraveyard(player1, "Insolent Neonate");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new InsolentNeonate());
        addCreatureReady(player2, new InsolentNeonate());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new InsolentNeonate());
        Permanent first = addCreatureReady(player2, new InsolentNeonate());
        Permanent second = addCreatureReady(player2, new InsolentNeonate());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void menaceAllowsNoBlockers() {
        addCreatureReady(player1, new InsolentNeonate());
        addCreatureReady(player2, new InsolentNeonate());
        declareAttackersAndPrepareBlockers(List.of(0));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }
}
