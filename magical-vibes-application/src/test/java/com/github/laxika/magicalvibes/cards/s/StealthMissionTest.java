package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.t.TotallyLost;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({StealthMission.class, PrimordialWurm.class, Forest.class, TotallyLost.class})
class StealthMissionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on the target and makes it unblockable")
    void putsCountersAndMakesTargetUnblockable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        castStealthMission(creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The unblockable effect wears off at end of turn, but the counters remain")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        castStealthMission(creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only targets creatures you control")
    void rejectsIllegalTargets() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        assertThatThrownBy(() -> castStealthMission(opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        UUID forestId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();
        assertThatThrownBy(() -> castStealthMission(forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The target cannot be blocked in combat")
    void preventsBlockingInCombat() {
        Permanent creature = addCreatureReady(player1, new PrimordialWurm());
        addCreatureReady(player2, new PrimordialWurm());
        castStealthMission(creature.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Repeated casts add counters only to the chosen creature")
    void repeatedCastsAccumulateCountersOnlyOnTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());

        castStealthMission(creature.getId());
        castStealthMission(creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(creature.isCantBeBlocked()).isTrue();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.isCantBeBlocked()).isFalse();
    }

    @Test
    @CardUsed(TotallyLost.class)
    @DisplayName("Neither effect applies if the target leaves before resolution")
    void targetLeavingBeforeResolutionPreventsBothEffects() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.setHand(player1, List.of(new StealthMission()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new TotallyLost()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primordial Wurm");
        harness.assertInGraveyard(player1, "Stealth Mission");
        assertThat(gd.playerDecks.get(player1.getId())).first().isSameAs(creature.getCard());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.isCantBeBlocked()).isFalse();
    }

    private void castStealthMission(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StealthMission()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
