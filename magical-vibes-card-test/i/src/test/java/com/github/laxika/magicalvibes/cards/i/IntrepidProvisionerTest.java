package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.cards.e.EpitaphGolem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntrepidProvisioner.class, ThrabenInspector.class, EpitaphGolem.class, InfernalGrasp.class})
class IntrepidProvisionerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another Human you control +2/+2")
    void etbBoostsAnotherHumanYouControl() {
        harness.addToBattlefield(player1, new ThrabenInspector());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID humanId = harness.getPermanentId(player1, "Thraben Inspector");
        harness.castCreature(player1, 0, humanId);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        Permanent human = findPermanent(player1, "Thraben Inspector");
        assertThat(human.getPowerModifier()).isEqualTo(2);
        assertThat(human.getToughnessModifier()).isEqualTo(2);
        assertThat(human.getEffectivePower()).isEqualTo(3);
        assertThat(human.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ThrabenInspector());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID humanId = harness.getPermanentId(player1, "Thraben Inspector");
        harness.castCreature(player1, 0, humanId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent human = findPermanent(player1, "Thraben Inspector");
        assertThat(human.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(human.getPowerModifier()).isEqualTo(0);
        assertThat(human.getToughnessModifier()).isEqualTo(0);
        assertThat(human.getEffectivePower()).isEqualTo(1);
        assertThat(human.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects non-Human as target")
    void rejectsNonHumanTarget() {
        harness.addToBattlefield(player1, new EpitaphGolem());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID golemId = harness.getPermanentId(player1, "Epitaph Golem");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, golemId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Human you control");
    }

    @Test
    @DisplayName("Rejects opponent's Human as target")
    void rejectsOpponentsHuman() {
        harness.addToBattlefield(player2, new ThrabenInspector());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID humanId = harness.getPermanentId(player2, "Thraben Inspector");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, humanId))
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

        harness.assertOnBattlefield(player1, "Intrepid Provisioner");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving creature puts ETB trigger on stack with target")
    void resolvingPutsEtbOnStack() {
        harness.addToBattlefield(player1, new ThrabenInspector());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID humanId = harness.getPermanentId(player1, "Thraben Inspector");
        harness.castCreature(player1, 0, humanId);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(humanId);
    }

    @Test
    @DisplayName("Another Intrepid Provisioner is a legal target")
    void boostsAnotherProvisionerWithoutBoostingItself() {
        harness.addToBattlefield(player1, new IntrepidProvisioner());
        Permanent other = findPermanent(player1, "Intrepid Provisioner");
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, other.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(other.getEffectivePower()).isEqualTo(5);
        assertThat(other.getEffectiveToughness()).isEqualTo(5);
        Permanent source = findPermanents(player1, "Intrepid Provisioner").stream()
                .filter(p -> !p.getId().equals(other.getId()))
                .findFirst().orElseThrow();
        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Removing the target in response does not boost the source")
    void removedTargetDoesNotReceiveBoost() {
        harness.addToBattlefield(player1, new ThrabenInspector());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.BLACK, 2);
        UUID humanId = harness.getPermanentId(player1, "Thraben Inspector");

        harness.castCreature(player1, 0, humanId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, humanId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thraben Inspector");
        Permanent source = findPermanent(player1, "Intrepid Provisioner");
        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost resolves even if its source leaves the battlefield")
    void triggerResolvesAfterSourceIsDestroyed() {
        harness.addToBattlefield(player1, new ThrabenInspector());
        harness.setHand(player1, List.of(new IntrepidProvisioner()));
        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.BLACK, 2);
        UUID humanId = harness.getPermanentId(player1, "Thraben Inspector");

        harness.castCreature(player1, 0, humanId);
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Intrepid Provisioner");
        harness.castInstant(player2, 0, sourceId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Intrepid Provisioner");
        Permanent human = findPermanent(player1, "Thraben Inspector");
        assertThat(human.getEffectivePower()).isEqualTo(3);
        assertThat(human.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
