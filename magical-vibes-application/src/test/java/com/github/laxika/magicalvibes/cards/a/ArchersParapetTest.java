package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchersParapet.class})
class ArchersParapetTest extends BaseCardTest {

    @Test
    void activationMakesEachOpponentLoseLifeAndTapsSource() {
        Permanent parapet = addReadyParapet();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(parapet.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        addReadyParapet();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutGenericMana() {
        addReadyParapet();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent parapet = addReadyParapet();
        parapet.tap();
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ArchersParapet());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapIsPaidImmediatelyButLifeLossWaitsForResolution() {
        Permanent parapet = addReadyParapet();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(parapet.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent parapet = addReadyParapet();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(parapet);
        gd.playerGraveyards.get(player1.getId()).add(parapet.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void opponentsAreRelativeToAbilityController() {
        Permanent parapet = harness.addToBattlefieldAndReturn(player2, new ArchersParapet());
        parapet.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void defenderPreventsAttackingEvenWhenReady() {
        Permanent parapet = addReadyParapet();

        assertThat(als.canAttack(gd, parapet, player1.getId())).isFalse();
    }

    private Permanent addReadyParapet() {
        Permanent parapet = harness.addToBattlefieldAndReturn(player1, new ArchersParapet());
        parapet.setSummoningSick(false);
        return parapet;
    }
}
