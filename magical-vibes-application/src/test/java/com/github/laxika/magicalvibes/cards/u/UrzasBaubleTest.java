package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzasBauble.class, GrizzlyBears.class})
class UrzasBaubleTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices the Bauble and schedules a draw at the next upkeep")
    void activatingSacrificesAndSchedulesDraw() {
        harness.addToBattlefield(player1, new UrzasBauble());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Urza's Bauble");
        harness.assertInGraveyard(player1, "Urza's Bauble");

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.addToBattlefield(player1, new UrzasBauble());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Activating shows a random card from the target player's hand only to the controller")
    void activatingLooksAtRandomCardOnlyToController() {
        harness.addToBattlefield(player1, new UrzasBauble());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"))
                .noneMatch(log -> log.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("An empty target hand does not prevent the delayed draw from being scheduled")
    void emptyTargetHandStillSchedulesDraw() {
        harness.addToBattlefield(player1, new UrzasBauble());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("The sacrifice is paid before resolution and the draw is not immediate")
    void sacrificeIsACostAndDrawIsDelayed() {
        harness.addToBattlefield(player1, new UrzasBauble());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Urza's Bauble");
        harness.assertInGraveyard(player1, "Urza's Bauble");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("The controller may target their own hand")
    void canTargetOwnHand() {
        harness.addToBattlefield(player1, new UrzasBauble());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Only one card is shown from a hand containing multiple cards")
    void looksAtExactlyOneCardWithoutMovingIt() {
        harness.addToBattlefield(player1, new UrzasBauble());
        GrizzlyBears bears = new GrizzlyBears();
        UrzasBauble bauble = new UrzasBauble();
        harness.setHand(player2, List.of(bears, bauble));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .singleElement()
                .satisfies(message -> assertThat(message.contains(bears.getId().toString())
                        ^ message.contains(bauble.getId().toString())).isTrue());
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears, bauble);
    }

    @Test
    @DisplayName("Activation during upkeep waits for the next turn and the draw uses the stack")
    void upkeepActivationWaitsForNextTurn() {
        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new UrzasBauble());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
