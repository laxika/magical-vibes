package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SchoolDaze.class, GrizzlyBears.class})
class SchoolDazeTest extends BaseCardTest {

    @Test
    @DisplayName("Do Homework draws three cards")
    void doHomeworkDrawsThreeCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new SchoolDaze()));
        addMana(player1);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Fight Crime counters a spell and draws a card")
    void fightCrimeCountersAndDraws() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new SchoolDaze()));
        addMana(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{1}, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fight Crime cannot target a permanent")
    void fightCrimeCannotTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SchoolDaze()));
        addMana(player1);

        UUID permanentId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, new int[]{1}, permanentId, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fight Crime requires a spell target")
    void fightCrimeRequiresSpellTarget() {
        harness.setHand(player1, List.of(new SchoolDaze()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, new int[]{1}, null, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "School Daze");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fight Crime can counter its controller's instant")
    void fightCrimeCanCounterOwnInstant() {
        SchoolDaze homework = new SchoolDaze();
        SchoolDaze drawnCard = new SchoolDaze();
        harness.setHand(player1, List.of(homework, new SchoolDaze()));
        harness.setLibrary(player1, List.of(drawnCard, new SchoolDaze(), new SchoolDaze()));
        addMana(player1);
        addMana(player1);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.castModalInstantWithModes(player1, 0, 1, new int[]{1}, homework.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(homework).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fight Crime does not draw if its only target leaves the stack")
    void fightCrimeDoesNotDrawWhenTargetLeavesStack() {
        SchoolDaze homework = new SchoolDaze();
        SchoolDaze opponentDraw = new SchoolDaze();
        harness.setHand(player1, List.of(homework, new SchoolDaze()));
        harness.setLibrary(player1, List.of(new SchoolDaze(), new SchoolDaze(), new SchoolDaze()));
        harness.setHand(player2, List.of(new SchoolDaze()));
        harness.setLibrary(player2, List.of(opponentDraw));
        addMana(player1);
        addMana(player1);
        addMana(player2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{1}, homework.getId(), List.of());
        harness.castModalInstantWithModes(player1, 0, 1, new int[]{1}, homework.getId(), List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(homework).hasSize(2);
        harness.assertInGraveyard(player2, "School Daze");
        assertThat(gd.stack).isEmpty();
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
