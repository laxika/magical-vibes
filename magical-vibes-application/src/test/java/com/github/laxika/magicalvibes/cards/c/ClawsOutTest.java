package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrideSovereign;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClawsOutTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast with insufficient mana when no Cats are controlled")
    void cannotCastWithoutCats() {
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity for Cats reduces the cost for each Cat controlled")
    void affinityReducesCostForEachCatControlled() {
        harness.addToBattlefield(player1, new PrideSovereign());
        harness.addToBattlefield(player1, new PrideSovereign());
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(1);

        harness.castInstant(player1, 0, (UUID) null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cats controlled by an opponent do not reduce the cost")
    void opponentCatsDoNotReduceCost() {
        harness.addToBattlefield(player2, new PrideSovereign());
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Boosts your creatures but not your opponent's creatures")
    void boostsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new PrideSovereign());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(2);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(permanentOf(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(4);
        assertThat(permanentOf(player1, "Grizzly Bears").getEffectiveToughness()).isEqualTo(4);
        assertThat(permanentOf(player2, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
        assertThat(permanentOf(player2, "Grizzly Bears").getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new PrideSovereign());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(2);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = permanentOf(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    private void addClawsOutMana(int colorless) {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }

    private Permanent permanentOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
