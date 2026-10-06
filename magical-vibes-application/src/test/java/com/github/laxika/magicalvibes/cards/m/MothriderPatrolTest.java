package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MothriderPatrol.class})
class MothriderPatrolTest extends BaseCardTest {

    @Test
    void tapsTargetCreature() {
        Permanent patrol = addReadyPatrol(player1);
        Permanent target = addReadyPatrol(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(patrol.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutFullManaCost() {
        addReadyPatrol(player1);
        Permanent target = addReadyPatrol(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetPlayer() {
        addReadyPatrol(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTapAnotherCreatureYouControl() {
        Permanent patrol = addReadyPatrol(player1);
        Permanent target = addReadyPatrol(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetItself() {
        Permanent patrol = addReadyPatrol(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, patrol.getId());
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAnAlreadyTappedCreature() {
        Permanent patrol = addReadyPatrol(player1);
        Permanent target = addReadyPatrol(player2);
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent patrol = addReadyPatrol(player1);
        Permanent target = addReadyPatrol(player2);
        patrol.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent patrol = addReadyPatrol(player1);
        Permanent target = addReadyPatrol(player2);
        patrol.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(patrol.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent patrol = addReadyPatrol(player1);
        Permanent target = addReadyPatrol(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(patrol);
        gd.playerGraveyards.get(player1.getId()).add(patrol.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyPatrol(Player player) {
        return addCreatureReady(player, new MothriderPatrol());
    }
}
