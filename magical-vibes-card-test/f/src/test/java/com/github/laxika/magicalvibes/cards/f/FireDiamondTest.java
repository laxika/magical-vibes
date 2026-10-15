package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireDiamond.class})
class FireDiamondTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new FireDiamond(), "{2}");
        harness.passBothPriorities();

        Permanent diamond = findPermanent(player1, "Fire Diamond");
        assertThat(diamond.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for mana adds red mana")
    void tapForRedMana() {
        Permanent diamond = harness.addToBattlefieldAndReturn(player1, new FireDiamond());
        diamond.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(diamond.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the mana ability while tapped")
    void cannotTapForRedManaWhileTapped() {
        Permanent diamond = harness.addToBattlefieldAndReturn(player1, new FireDiamond());
        diamond.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield without being cast")
    void entersTappedWithoutBeingCast() {
        Permanent diamond = harness.enterBattlefieldAndReturn(player1, new FireDiamond());

        assertThat(diamond.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly cast Diamond can produce mana immediately after being untapped")
    void newlyCastDiamondCanProduceManaAfterUntapping() {
        harness.castFromHand(player1, new FireDiamond(), "{2}");
        harness.passBothPriorities();
        Permanent diamond = findPermanent(player1, "Fire Diamond");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        diamond.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(diamond.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
