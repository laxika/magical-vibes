package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampireNeonate.class})
class VampireNeonateTest extends BaseCardTest {

    @Test
    void activationMakesEachOpponentLoseLifeAndControllerGainLife() {
        Permanent neonate = addCreatureReady(player1, new VampireNeonate());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(neonate.isTapped()).isTrue();
    }

    @Test
    void lifeTotalsChangeOnlyWhenAbilityResolvesEvenIfSourceLeavesBattlefield() {
        Permanent neonate = addCreatureReady(player1, new VampireNeonate());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(neonate.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(neonate);
        gd.playerGraveyards.get(player1.getId()).add(neonate.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent neonate = addCreatureReady(player1, new VampireNeonate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(neonate.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent neonate = addCreatureReady(player1, new VampireNeonate());
        neonate.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent neonate = harness.addToBattlefieldAndReturn(player1, new VampireNeonate());
        neonate.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(neonate.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
