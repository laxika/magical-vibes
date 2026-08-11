package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarduHatebladeTest extends BaseCardTest {

    @Test
    void activatingAbilityGrantsDeathtouchUntilEndOfTurn() {
        Permanent hateblade = addHatebladeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hateblade, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void grantedDeathtouchWearsOffAtEndOfTurn() {
        Permanent hateblade = addHatebladeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hateblade, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void cannotActivateAbilityWithoutBlackMana() {
        addHatebladeReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void deathtouchKillsLargerBlocker() {
        Permanent hateblade = addHatebladeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.w.WurmcoilEngine());
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getLast();
        blocker.setSummoningSick(false);

        hateblade.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    private Permanent addHatebladeReady(Player player) {
        Permanent permanent = new Permanent(new MarduHateblade());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
