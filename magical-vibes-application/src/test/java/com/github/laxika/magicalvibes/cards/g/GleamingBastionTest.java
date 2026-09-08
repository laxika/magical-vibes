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
        Permanent bastion = new Permanent(new GleamingBastion());
        bastion.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bastion);
        return bastion;
    }
}
