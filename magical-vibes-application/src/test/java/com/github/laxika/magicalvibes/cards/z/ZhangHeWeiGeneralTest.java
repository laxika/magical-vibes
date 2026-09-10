package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.w.WeiEliteCompanions;
import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhangHeWeiGeneral.class, WeiEliteCompanions.class, WeiInfantry.class})
class ZhangHeWeiGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new ZhangHeWeiGeneral());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard() instanceof ZhangHeWeiGeneral);
    }

    @Test
    @DisplayName("Each other creature you control gets +1/+0 when Zhang He attacks")
    void otherCreaturesGetBoost() {
        addCreatureReady(player1, new ZhangHeWeiGeneral());
        Permanent other = addCreatureReady(player1, new WeiEliteCompanions());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(1);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Zhang He does not boost itself")
    void doesNotBoostItself() {
        Permanent zhangHe = addCreatureReady(player1, new ZhangHeWeiGeneral());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(zhangHe.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's creatures are not boosted")
    void opponentCreaturesNotBoosted() {
        addCreatureReady(player1, new ZhangHeWeiGeneral());
        Permanent enemy = addCreatureReady(player2, new WeiEliteCompanions());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(enemy.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ZhangHeWeiGeneral());
        Permanent other = addCreatureReady(player1, new WeiEliteCompanions());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isEqualTo(0);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger Zhang He's ability")
    void attackingAnotherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new ZhangHeWeiGeneral());
        Permanent other = addCreatureReady(player1, new WeiEliteCompanions());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Horsemanship: Zhang He can't be blocked by a creature without horsemanship")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new WeiInfantry());
        Permanent zhangHe = addCreatureReady(player1, new ZhangHeWeiGeneral());
        zhangHe.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zhangHe);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Horsemanship: Zhang He can be blocked by a creature with horsemanship")
    void canBeBlockedByCreatureWithHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new WeiEliteCompanions());
        Permanent zhangHe = addCreatureReady(player1, new ZhangHeWeiGeneral());
        zhangHe.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zhangHe);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
