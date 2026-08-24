package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkarrganHellkiteTest extends BaseCardTest {

    @Test
    void riotAddsCounterWhenChosen() {
        harness.setHand(player1, List.of(new SkarrganHellkite()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent hellkite = findHellkite();
        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutPlusOnePlusOneCounter() {
        Permanent hellkite = addReadyHellkite();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(target.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hellkite.isTapped()).isFalse();
    }

    @Test
    void dealsTwoDamageToOneTargetWithCounter() {
        Permanent hellkite = addReadyHellkite();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(target.getId(), 2));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void dividesTwoDamageBetweenCreatureAndPlayer() {
        Permanent hellkite = addReadyHellkite();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent creatureTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(creatureTarget.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(creatureTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    private Permanent addReadyHellkite() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new SkarrganHellkite());
        hellkite.setSummoningSick(false);
        return hellkite;
    }

    private Permanent findHellkite() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SkarrganHellkite)
                .findFirst()
                .orElseThrow();
    }
}
