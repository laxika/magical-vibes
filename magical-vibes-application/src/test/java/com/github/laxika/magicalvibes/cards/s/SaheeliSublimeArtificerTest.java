package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Prismite;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaheeliSublimeArtificer.class, Shock.class, GrizzlyBears.class, WornPowerstone.class, Prismite.class})
class SaheeliSublimeArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("creates a Servo when you cast a noncreature spell")
    void createsServoForNoncreatureSpell() {
        addReadySaheeli(5);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent servo = findPermanents(player1, "Servo").getFirst();
        assertThat(servo.getCard().isToken()).isTrue();
        assertThat(gqs.isArtifact(gd, servo)).isTrue();
        assertThat(gqs.isCreature(gd, servo)).isTrue();
    }

    @Test
    @DisplayName("does not create a Servo when you cast a creature spell")
    void doesNotCreateServoForCreatureSpell() {
        addReadySaheeli(5);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Servo")).isEmpty();
    }

    @Test
    @DisplayName("copies a creature while keeping the target an artifact")
    void copiesCreatureWithArtifactException() {
        Permanent saheeli = addReadySaheeli(5);
        Permanent powerstone = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, indexOf(saheeli), 0,
                List.of(powerstone.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, powerstone)).isTrue();
        assertThat(gqs.isCreature(gd, powerstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, powerstone)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, powerstone)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, powerstone)).isTrue();
        assertThat(gqs.isCreature(gd, powerstone)).isFalse();
    }

    @Test
    @DisplayName("copies an artifact onto a Servo while preserving its token status")
    void copiesArtifactOntoServo() {
        Permanent saheeli = addReadySaheeli(5);
        Permanent powerstone = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        Permanent servo = createServo();

        harness.activateAbilityWithMultiTargets(player1, indexOf(saheeli), 0,
                List.of(servo.getId(), powerstone.getId()));
        harness.passBothPriorities();

        assertThat(servo.getCard().isToken()).isTrue();
        assertThat(gqs.isArtifact(gd, servo)).isTrue();
        harness.activateAbility(player1, indexOf(servo), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void artifactCreatureSpellDoesNotCreateServo() {
        addReadySaheeli(5);
        harness.setHand(player1, List.of(new Prismite()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Servo")).isEmpty();
        harness.assertOnBattlefield(player1, "Prismite");
    }

    @Test
    void opponentsNoncreatureSpellDoesNotCreateServo() {
        addReadySaheeli(5);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Servo")).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    void servoResolvesBeforeSpellAndHasOnePowerAndToughness() {
        addReadySaheeli(5);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Servo")).hasSize(1);
        Permanent servo = findPermanent(player1, "Servo");
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        harness.assertLife(player2, 20);
        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    void copyDoesNotCopyCountersAndKeepsItsOwnCounters() {
        Permanent saheeli = addReadySaheeli(5);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Prismite());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Prismite());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbilityWithMultiTargets(player1, indexOf(saheeli), 0,
                List.of(target.getId(), source.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(saheeli.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void cannotCopyArtifactOntoItself() {
        Permanent saheeli = addReadySaheeli(5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Prismite());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, indexOf(saheeli), 0,
                List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(saheeli.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void doesNotCopySourceThatDiesBeforeResolution() {
        Permanent saheeli = addReadySaheeli(5);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Prismite());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbilityWithMultiTargets(player1, indexOf(saheeli), 0,
                List.of(target.getId(), source.getId()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, source.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Prismite");
        assertThat(gqs.isCreature(gd, target)).isFalse();
        assertThat(target.getCard().getName()).isEqualTo("Worn Powerstone");
    }

    @Test
    void cannotCopyOpponentsPermanent() {
        Permanent saheeli = addReadySaheeli(5);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Prismite());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new Prismite());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, indexOf(saheeli), 0,
                List.of(target.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(saheeli.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void firstTargetMustBeAnArtifactYouControl() {
        Permanent saheeli = addReadySaheeli(5);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Prismite());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Prismite());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, indexOf(saheeli), 0,
                List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, indexOf(saheeli), 0,
                List.of(opposingArtifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(saheeli.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    private Permanent addReadySaheeli(int loyalty) {
        Permanent saheeli = harness.addToBattlefieldAndReturn(player1, new SaheeliSublimeArtificer());
        saheeli.setCounterCount(CounterType.LOYALTY, loyalty);
        saheeli.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return saheeli;
    }

    private Permanent createServo() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        return findPermanents(player1, "Servo").getFirst();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
