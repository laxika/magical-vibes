package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReduceRubbleTest extends BaseCardTest {

    @Test
    @DisplayName("Reduce counters spell when opponent cannot pay {3}")
    void reduceCountersWhenOpponentCannotPay() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new ReduceRubble()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Reduce");
    }

    @Test
    @DisplayName("Reduce does not counter when opponent pays {3}")
    void reduceDoesNotCounterWhenOpponentPays() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new ReduceRubble()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Reduce");
    }

    @Test
    @DisplayName("Rubble from graveyard locks up to three lands then exiles")
    void rubbleLocksLandsThenExiles() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setGraveyard(player1, List.of(new ReduceRubble()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFlashback(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
        assertThat(third.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Reduce") || c.getName().equals("Rubble"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reduce"));
    }

    @Test
    @DisplayName("Rubble may target fewer than three lands")
    void rubbleMayTargetFewerLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setGraveyard(player1, List.of(new ReduceRubble()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFlashback(player1, 0, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(land.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rubble cannot target a nonland permanent")
    void rubbleRejectsNonland() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ReduceRubble()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rubble requires sorcery timing")
    void rubbleRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new ReduceRubble()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }
}
