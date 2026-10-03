package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AjaniWiseCounselor.class, GreenwoodSentinel.class})
class AjaniWiseCounselorTest extends BaseCardTest {

    @Test
    @DisplayName("+2 gains one life for each creature controlled")
    void plusTwoGainsLifeForControlledCreatures() {
        Permanent ajani = addReadyAjani(player1, 5);
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        int lifeBefore = gd.getLife(player1.getId());
        activate(ajani, 0);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("−3 boosts creatures you control until end of turn")
    void minusThreeBoostsOwnCreatures() {
        Permanent ajani = addReadyAjani(player1, 5);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        activate(ajani, 1);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("−9 puts counters equal to life total on target creature")
    void minusNinePutsLifeTotalCountersOnTargetCreature() {
        Permanent ajani = addReadyAjani(player1, 9);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 7);

        activate(ajani, 2, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(9);
    }

    @Test
    void plusTwoWithNoCreaturesGainsNoLife() {
        Permanent ajani = addReadyAjani(player1, 5);
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        int lifeBefore = gd.getLife(player1.getId());

        activate(ajani, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void plusTwoCountsCreaturesAtResolution() {
        Permanent ajani = addReadyAjani(player1, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        int lifeBefore = gd.getLife(player1.getId());

        activate(ajani, 0);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void minusThreeDoesNotBoostCreaturesEnteringAfterResolutionAndExpires() {
        Permanent ajani = addReadyAjani(player1, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        activate(ajani, 1);
        harness.passBothPriorities();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void minusNineUsesControllerLifeAtResolutionAfterAjaniLeaves() {
        Permanent ajani = addReadyAjani(player1, 9);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setLife(player1, 7);
        harness.setLife(player2, 15);

        activate(ajani, 2, creature.getId());
        harness.setLife(player1, 11);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ajani);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(11);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(13);
    }

    @Test
    void minusNineDoesNotAffectCreatureThatLeavesAndReturns() {
        Permanent ajani = addReadyAjani(player1, 9);
        GreenwoodSentinel card = new GreenwoodSentinel();
        Permanent original = harness.addToBattlefieldAndReturn(player2, card);

        activate(ajani, 2, original.getId());
        gd.playerBattlefields.get(player2.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, card);
        harness.passBothPriorities();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void minusNineCannotTargetANoncreature() {
        Permanent ajani = addReadyAjani(player1, 10);

        assertThatThrownBy(() -> activate(ajani, 2, ajani.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void minusNineCannotBeActivatedWithInsufficientLoyalty() {
        Permanent ajani = addReadyAjani(player1, 8);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        assertThatThrownBy(() -> activate(ajani, 2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void activate(Permanent ajani, int abilityIndex) {
        activate(ajani, abilityIndex, null);
    }

    private void activate(Permanent ajani, int abilityIndex, java.util.UUID targetId) {
        int ajaniIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
        harness.activateAbility(player1, ajaniIndex, abilityIndex, null, targetId);
    }

    private Permanent addReadyAjani(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AjaniWiseCounselor());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
