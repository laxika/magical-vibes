package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IronHillsStalwart;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GandalfSparkStarter.class, IronHillsStalwart.class})
class GandalfSparkStarterTest extends BaseCardTest {

    @Test
    void entersAndDealsThreeDamageToOneTarget() {
        harness.setLife(player2, 20);
        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 3);

        castGandalf();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void dividesThreeDamageAmongTwoTargets() {
        harness.setLife(player2, 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IronHillsStalwart());
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 1, player2.getId(), 2);

        castGandalf();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void dividesThreeDamageAmongThreeTargets() {
        harness.setLife(player2, 20);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new IronHillsStalwart());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new IronHillsStalwart());
        gd.pendingETBDamageAssignments = Map.of(
                firstCreature.getId(), 1,
                secondCreature.getId(), 1,
                player2.getId(), 1
        );

        castGandalf();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(secondCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void asksForTargetsAndDivisionWhenMandatoryAbilityTriggers() {
        castGandalf();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void retainsOriginalDivisionWhenOneTargetLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IronHillsStalwart());
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 2, player2.getId(), 1);

        castGandalf();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void abilityStillDealsDamageAfterGandalfLeavesBattlefield() {
        harness.setLife(player2, 20);
        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 3);

        castGandalf();
        harness.passBothPriorities();
        Permanent gandalf = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(gandalf);
        gd.playerGraveyards.get(player1.getId()).add(gandalf.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private void castGandalf() {
        harness.castFromHand(player1, new GandalfSparkStarter(), "{4}{R}{R}");
    }
}
