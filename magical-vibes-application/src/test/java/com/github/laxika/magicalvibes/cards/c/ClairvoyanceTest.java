package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Clairvoyance.class, BalduvianBears.class})
class ClairvoyanceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving looks at the target's hand and schedules a draw at the next upkeep")
    void looksAtHandAndSchedulesDraw() {
        harness.setHand(player2, List.of(new Clairvoyance(), new Clairvoyance()));
        harness.setHand(player1, List.of(new Clairvoyance()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Clairvoyance"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target self to look at own hand")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Clairvoyance()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Clairvoyance()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Balduvian Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only target players");
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Clairvoyance()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("An empty target hand does not prevent the delayed draw")
    void emptyTargetHandStillDraws() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Clairvoyance()));
        harness.setLibrary(player1, List.of(new BalduvianBears(), new Clairvoyance()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP));
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        harness.assertInHand(player1, "Balduvian Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting during upkeep waits until the next turn and triggers only once")
    void castDuringUpkeepWaitsUntilNextTurnAndDrawsOnlyOnce() {
        harness.forceActivePlayer(player1);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Clairvoyance()));
        harness.setLibrary(player1, List.of(new BalduvianBears(), new Clairvoyance(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        harness.castInstant(player1, 0, player2.getId());
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
