package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AjaniWiseCounselorTest extends BaseCardTest {

    @Test
    @DisplayName("+2 gains one life for each creature controlled")
    void plusTwoGainsLifeForControlledCreatures() {
        Permanent ajani = addReadyAjani(player1, 5);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

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
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerLifeTotals.put(player1.getId(), 7);

        activate(ajani, 2, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(9);
    }

    private void activate(Permanent ajani, int abilityIndex) {
        activate(ajani, abilityIndex, null);
    }

    private void activate(Permanent ajani, int abilityIndex, java.util.UUID targetId) {
        int ajaniIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
        harness.activateAbility(player1, ajaniIndex, abilityIndex, null, targetId);
    }

    private Permanent addReadyAjani(Player player, int loyalty) {
        Permanent perm = new Permanent(new AjaniWiseCounselor());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
