package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GleamingBastion.class, Plains.class})
class GleamingBastionTest extends BaseCardTest {

    @Test
    void producesWhiteManaWhenItEnteredThisTurn() {
        Permanent bastion = harness.enterBattlefieldAndReturn(player1, new GleamingBastion());
        bastion.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(bastion.isTapped()).isTrue();
    }

    @Test
    void producesBlueManaWhenItsControllerControlsABasicLand() {
        Permanent bastion = addReadyBastion();
        harness.addToBattlefield(player1, new Plains());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bastion.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutBasicLandOrHavingEnteredThisTurn() {
        Permanent bastion = addReadyBastion();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bastion.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    private Permanent addReadyBastion() {
        Permanent bastion = harness.addToBattlefieldAndReturn(player1, new GleamingBastion());
        bastion.setSummoningSick(false);
        return bastion;
    }

    @Test
    void producesColorlessManaWithoutBasicLandOrHavingEnteredThisTurn() {
        Permanent bastion = addReadyBastion();

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(bastion.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void producesBlueManaWhenItEnteredThisTurn() {
        Permanent bastion = harness.enterBattlefieldAndReturn(player1, new GleamingBastion());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bastion.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void producesWhiteManaWithATappedBasicLand() {
        Permanent bastion = addReadyBastion();
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        plains.tap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(bastion.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsBasicLandDoesNotEnableEitherColor() {
        Permanent bastion = addReadyBastion();
        harness.addToBattlefield(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bastion.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void anotherNonbasicLandDoesNotEnableBlueMana() {
        Permanent bastion = addReadyBastion();
        harness.addToBattlefield(player1, new GleamingBastion());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bastion.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }
}
