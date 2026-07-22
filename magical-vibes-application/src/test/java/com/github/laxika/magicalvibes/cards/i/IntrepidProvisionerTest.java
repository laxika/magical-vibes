package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntrepidProvisionerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another Human you control +2/+2")
    void etbBoostsAnotherHumanYouControl() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID humanId = harness.getPermanentId(player1, "Elite Vanguard");
        gs.playCard(gd, player1, 0, 0, humanId, null);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        Permanent human = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(humanId))
                .findFirst().orElseThrow();
        assertThat(human.getPowerModifier()).isEqualTo(2);
        assertThat(human.getToughnessModifier()).isEqualTo(2);
        assertThat(human.getEffectivePower()).isEqualTo(4);
        assertThat(human.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID humanId = harness.getPermanentId(player1, "Elite Vanguard");
        gs.playCard(gd, player1, 0, 0, humanId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent human = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(humanId))
                .findFirst().orElseThrow();
        assertThat(human.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(human.getPowerModifier()).isEqualTo(0);
        assertThat(human.getToughnessModifier()).isEqualTo(0);
        assertThat(human.getEffectivePower()).isEqualTo(2);
        assertThat(human.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects non-Human as target")
    void rejectsNonHumanTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Human you control");
    }

    @Test
    @DisplayName("Rejects opponent's Human as target")
    void rejectsOpponentsHuman() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID humanId = harness.getPermanentId(player2, "Elite Vanguard");
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, humanId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Human you control");
    }

    @Test
    @DisplayName("Can cast without a target when no other Human you control")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Intrepid Provisioner"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving creature puts ETB trigger on stack with target")
    void resolvingPutsEtbOnStack() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID humanId = harness.getPermanentId(player1, "Elite Vanguard");
        gs.playCard(gd, player1, 0, 0, humanId, null);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(humanId);
    }
}
