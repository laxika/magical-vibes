package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NestRobber;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RummagingGoblin.class, NestRobber.class, Forest.class, Mountain.class})
class RummagingGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability starts discard-cost choice for any card")
    void activationStartsDiscardChoice() {
        addCreatureReady(player1, new RummagingGoblin());
        harness.setHand(player1, List.of(new NestRobber(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
        // All cards should be valid since predicate is null (any card)
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(0, 1);
    }

    @Test
    @DisplayName("Choosing a card pays cost and puts ability on stack")
    void choosingCardPaysCostAndStacksAbility() {
        addCreatureReady(player1, new RummagingGoblin());
        harness.setHand(player1, List.of(new NestRobber(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotInHand(player1, "Nest Robber");
        harness.assertInGraveyard(player1, "Nest Robber");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Rummaging Goblin");
    }

    @Test
    @DisplayName("Cannot activate without cards in hand")
    void cannotActivateWithoutCardsInHand() {
        addCreatureReady(player1, new RummagingGoblin());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving ability draws a card")
    void resolvingDrawsACard() {
        addCreatureReady(player1, new RummagingGoblin());
        harness.setHand(player1, List.of(new NestRobber()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        // Hand had 1 card, discarded 1 as cost (hand = 0), then drew 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("Can discard any card type as cost, including lands")
    void canDiscardLandAsCost() {
        addCreatureReady(player1, new RummagingGoblin());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent goblin = addCreatureReady(player1, new RummagingGoblin());
        goblin.tap();
        harness.setHand(player1, List.of(new NestRobber()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new RummagingGoblin());
        perm.setSummoningSick(true);
        harness.setHand(player1, List.of(new NestRobber()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating ability taps Rummaging Goblin after paying discard cost")
    void activatingTapsGoblin() {
        Permanent goblin = addCreatureReady(player1, new RummagingGoblin());
        harness.setHand(player1, List.of(new NestRobber()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(goblin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Discard is paid immediately and the draw resolves after the source leaves")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent goblin = addCreatureReady(player1, new RummagingGoblin());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, goblin));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rummaging Goblin");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate on the opponent's turn and only the controller draws")
    void activatesOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        addCreatureReady(player1, new RummagingGoblin());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}