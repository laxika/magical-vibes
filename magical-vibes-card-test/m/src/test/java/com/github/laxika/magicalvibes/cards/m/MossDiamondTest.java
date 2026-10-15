package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MossDiamond.class})
class MossDiamondTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new MossDiamond(), "{2}");
        harness.passBothPriorities();

        Permanent diamond = findPermanent(player1, "Moss Diamond");
        assertThat(diamond.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for mana adds green mana")
    void tapForGreenMana() {
        Permanent diamond = harness.addToBattlefieldAndReturn(player1, new MossDiamond());
        diamond.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isGreaterThanOrEqualTo(1);
        assertThat(diamond.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the mana ability while tapped")
    void cannotTapForGreenManaWhileTapped() {
        Permanent diamond = harness.addToBattlefieldAndReturn(player1, new MossDiamond());
        diamond.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Untapped newly entered Diamond produces exactly one green immediately and must untap to activate again")
    void manaResolvesImmediatelyAndRequiresUntappingBetweenActivations() {
        Permanent diamond = harness.enterBattlefieldAndReturn(player1, new MossDiamond());
        assertThat(diamond.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        diamond.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(diamond.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        diamond.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(diamond.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
