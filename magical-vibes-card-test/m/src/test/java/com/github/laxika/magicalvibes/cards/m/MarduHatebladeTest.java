package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarduHateblade.class, AlpineGrizzly.class})
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

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        blocker.setSummoningSick(false);

        hateblade.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    void abilityUsesTheStackAndOnlyGrantsDeathtouchToItsSource() {
        Permanent hateblade = addHatebladeReady(player1);
        Permanent otherHateblade = addHatebladeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, hateblade, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherHateblade, Keyword.DEATHTOUCH)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hateblade, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherHateblade, Keyword.DEATHTOUCH)).isFalse();
        assertThat(hateblade.isTapped()).isFalse();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent hateblade = harness.addToBattlefieldAndReturn(player1, new MarduHateblade());
        hateblade.setSummoningSick(true);
        hateblade.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hateblade, Keyword.DEATHTOUCH)).isTrue();
        assertThat(hateblade.isTapped()).isTrue();
    }

    @Test
    void whiteManaCannotPayForTheAbility() {
        addHatebladeReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addHatebladeReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MarduHateblade());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
