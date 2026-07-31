package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        // Grizzly Bears costs {1}{G} and has no flying, so no reduction applies
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

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
}
