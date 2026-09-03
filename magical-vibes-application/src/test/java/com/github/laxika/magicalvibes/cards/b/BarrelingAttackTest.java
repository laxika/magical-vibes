package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrystalVein;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrelingAttack.class, CrystalVein.class, DarkBanishing.class, ViashinoWarrior.class})
class BarrelingAttackTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants trample to the target")
    void grantsTrample() {
        Permanent target = addCreatureReady(player1, new ViashinoWarrior());
        castBarrelingAttack(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOff() {
        Permanent target = addCreatureReady(player1, new ViashinoWarrior());
        castBarrelingAttack(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new CrystalVein());
        harness.setHand(player1, List.of(new BarrelingAttack()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID crystalVeinId = harness.getPermanentId(player1, "Crystal Vein");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, crystalVeinId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Becoming blocked by one creature gives the target +1/+1")
    void oneBlockerGivesPlusOnePlusOne() {
        Permanent target = addCreatureReady(player1, new ViashinoWarrior());
        addCreatureReady(player2, new ViashinoWarrior());
        castBarrelingAttack(target);

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked by two creatures gives the target +2/+2")
    void twoBlockersGivePlusTwoPlusTwo() {
        Permanent target = addCreatureReady(player1, new ViashinoWarrior());
        addCreatureReady(player2, new ViashinoWarrior());
        addCreatureReady(player2, new ViashinoWarrior());
        castBarrelingAttack(target);

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The blocking bonus wears off at end of turn")
    void blockingBonusWearsOff() {
        Permanent target = addCreatureReady(player1, new ViashinoWarrior());
        addCreatureReady(player2, new ViashinoWarrior());
        castBarrelingAttack(target);

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Staying unblocked gives no boost")
    void unblockedGivesNoBoost() {
        Permanent target = addCreatureReady(player1, new ViashinoWarrior());
        castBarrelingAttack(target);

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A creature that was not the target gets no boost when blocked")
    void untargetedCreatureGetsNoBoost() {
        Permanent target = addCreatureReady(player1, new ViashinoWarrior());
        Permanent other = addCreatureReady(player1, new ViashinoWarrior());
        addCreatureReady(player2, new ViashinoWarrior());
        castBarrelingAttack(target);

        other.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Casting after blockers are declared does not trigger the blocking bonus")
    void castingAfterBlockersAreDeclaredDoesNotTriggerBonus() {
        Permanent target = addCreatureReady(player1, new ViashinoWarrior());
        addCreatureReady(player2, new ViashinoWarrior());

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castBarrelingAttack(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blocker removed before the trigger resolves is not counted")
    void blockerRemovedBeforeTriggerResolvesIsNotCounted() {
        Permanent target = addCreatureReady(player1, new ViashinoWarrior());
        Permanent blocker = addCreatureReady(player2, new ViashinoWarrior());
        castBarrelingAttack(target);

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private void castBarrelingAttack(Permanent target) {
        harness.setHand(player1, List.of(new BarrelingAttack()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

}
