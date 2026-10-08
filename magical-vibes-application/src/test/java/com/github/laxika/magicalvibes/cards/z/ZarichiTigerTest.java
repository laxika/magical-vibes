package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.r.RapidHybridization;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZarichiTiger.class, RapidHybridization.class})
class ZarichiTigerTest extends BaseCardTest {

    @Test
    void payingManaAndTappingGainsTwoLife() {
        Permanent tiger = harness.addToBattlefieldAndReturn(player1, new ZarichiTiger());
        tiger.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(tiger.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ZarichiTiger());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent tiger = harness.addToBattlefieldAndReturn(player1, new ZarichiTiger());
        tiger.setSummoningSick(false);
        tiger.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        Permanent tiger = harness.addToBattlefieldAndReturn(player1, new ZarichiTiger());
        tiger.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(tiger.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutGenericManaPayment() {
        Permanent tiger = harness.addToBattlefieldAndReturn(player1, new ZarichiTiger());
        tiger.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(tiger.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterTigerIsDestroyedInResponse() {
        Permanent tiger = harness.addToBattlefieldAndReturn(player1, new ZarichiTiger());
        tiger.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, java.util.List.of(new RapidHybridization()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 20);
        harness.castAndResolveInstant(player2, 0, tiger.getId());

        harness.assertInGraveyard(player1, "Zarichi Tiger");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
