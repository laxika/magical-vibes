package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WardenOfEvosIsle.class, SerraAngel.class, CoralMerfolk.class, Divination.class})
class WardenOfEvosIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells with flying you cast cost {1} less")
    void flyingCreatureSpellIsReduced() {
        harness.addToBattlefield(player1, new WardenOfEvosIsle());
        // Serra Angel costs {3}{W}{W} — with the {1} reduction it costs {2}{W}{W}
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Serra Angel");
    }

    @Test
    @DisplayName("Without the Warden the same spell is not affordable")
    void noReductionWithoutWarden() {
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature spells without flying are not reduced")
    void nonFlyingCreatureSpellNotReduced() {
        harness.addToBattlefield(player1, new WardenOfEvosIsle());
        // Coral Merfolk costs {1}{U} and has no flying, so no reduction applies
        harness.setHand(player1, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponents' flying creature spells are not reduced")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new WardenOfEvosIsle());
        harness.setHand(player2, List.of(new SerraAngel()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Wardens reduce the same flying creature spell cumulatively")
    void multipleWardensStack() {
        harness.addToBattlefield(player1, new WardenOfEvosIsle());
        harness.addToBattlefield(player1, new WardenOfEvosIsle());
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Serra Angel");
    }

    @Test
    @DisplayName("Excess reductions cannot remove colored mana requirements")
    void excessReductionDoesNotPayColoredMana() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new WardenOfEvosIsle());
        }
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A Warden being cast does not reduce its own cost")
    void wardenDoesNotReduceItselfFromHand() {
        harness.setHand(player1, List.of(new WardenOfEvosIsle()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Warden on the battlefield reduces another Warden spell")
    void wardenReducesAnotherWarden() {
        harness.addToBattlefield(player1, new WardenOfEvosIsle());
        harness.setHand(player1, List.of(new WardenOfEvosIsle()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Warden of Evos Isle");
    }

    @Test
    @DisplayName("A Warden in the graveyard does not reduce flying creature spells")
    void graveyardWardenDoesNotReduceCosts() {
        harness.setGraveyard(player1, List.of(new WardenOfEvosIsle()));
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature spells are not reduced")
    void noncreatureSpellNotReduced() {
        harness.addToBattlefield(player1, new WardenOfEvosIsle());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
