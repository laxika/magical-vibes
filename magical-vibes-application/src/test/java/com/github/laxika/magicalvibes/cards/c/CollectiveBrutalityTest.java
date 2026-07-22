package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectiveBrutalityTest extends BaseCardTest {

    // Modes: 0 = discard I/S from hand, 1 = creature -2/-2, 2 = drain 2

    @Test
    @DisplayName("Creature mode: target gets -2/-2 until end of turn")
    void creatureModeGivesMinusTwoMinusTwo() {
        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(4);
        bigBear.setToughness(4);
        Permanent bears = addCreatureReady(player2, bigBear);
        harness.setHand(player1, List.of(new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(bears.getId()), null);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(bears.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Drain mode: opponent loses 2 life and controller gains 2")
    void drainModeDrainsTwo() {
        harness.setHand(player1, List.of(new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Hand mode: prompts to discard an instant or sorcery from opponent's hand")
    void handModeDiscardsInstantOrSorcery() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new CollectiveBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Peek"))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Two modes: escalate discards one card and both modes resolve")
    void twoModesEscalateAndResolve() {
        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(4);
        bigBear.setToughness(4);
        Permanent bears = addCreatureReady(player2, bigBear);
        harness.setHand(player1, List.of(new CollectiveBrutality(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        // Discard Shock (hand index 1) to escalate for the second mode
        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1, 2},
                List.of(bears.getId(), player2.getId()), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"))
                .anyMatch(c -> c.getName().equals("Collective Brutality"));
        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Two modes without escalate discard is rejected")
    void twoModesWithoutDiscardRejected() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveBrutality(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() ->
                harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1, 2},
                        List.of(bears.getId(), player2.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
