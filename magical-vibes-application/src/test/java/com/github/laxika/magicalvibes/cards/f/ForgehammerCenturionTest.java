package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForgehammerCenturionTest extends BaseCardTest {

    @Test
    @DisplayName("Gets an oil counter when a creature or artifact you control dies")
    void getsOilCounterWhenOwnCreatureOrArtifactDies() {
        Permanent centurion = addCenturion();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(centurion.getCounterCount(CounterType.OIL)).isEqualTo(1);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(centurion.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void ignoresOpponentCreature() {
        Permanent centurion = addCenturion();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(centurion.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("May remove two oil counters on attack to stop a creature from blocking")
    void paysOilCountersOnAttack() {
        Permanent centurion = addCenturion();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        centurion.setCounterCount(CounterType.OIL, 2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(centurion.getCounterCount(CounterType.OIL)).isZero();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the attack ability with fewer than two oil counters")
    void cannotPayWithOneOilCounter() {
        Permanent centurion = addCenturion();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        centurion.setCounterCount(CounterType.OIL, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(centurion.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The blocking restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent centurion = addCenturion();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        centurion.setCounterCount(CounterType.OIL, 2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    private Permanent addCenturion() {
        return addCreatureReady(player1, new ForgehammerCenturion());
    }
}
