package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SaheeliSublimeArtificer.class, Shock.class, GrizzlyBears.class, WornPowerstone.class})
class SaheeliSublimeArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("creates a Servo when you cast a noncreature spell")
    void createsServoForNoncreatureSpell() {
        addReadySaheeli(5);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanents(player1, "Servo").getFirst();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
