package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AkkiCoalflingerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants first strike to every attacking creature, including the opponent's")
    void grantsFirstStrikeToAttackers() {
        addReadyCoalflinger(player1);
        Permanent ownAttacker = addBears(player1, true);
        Permanent opponentAttacker = addBears(player2, true);
        Permanent idleBears = addBears(player1, false);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownAttacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentAttacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, idleBears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void grantWearsOff() {
        addReadyCoalflinger(player1);
        Permanent attacker = addBears(player1, true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Creatures that start attacking after the ability resolves do not gain first strike")
    void grantDoesNotApplyToLaterAttackers() {
        addReadyCoalflinger(player1);
        Permanent lateAttacker = addBears(player1, false);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        lateAttacker.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, lateAttacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addReadyCoalflinger(Player player) {
        Permanent perm = new Permanent(new AkkiCoalflinger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBears(Player player, boolean attacking) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(attacking);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
