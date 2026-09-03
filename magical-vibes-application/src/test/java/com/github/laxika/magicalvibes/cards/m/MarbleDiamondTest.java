package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MarbleDiamond.class)
class MarbleDiamondTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new MarbleDiamond(), "{2}");
        harness.passBothPriorities();

        Permanent diamond = findDiamond(player1);
        assertThat(diamond.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for mana adds white mana")
    void tapForWhiteMana() {
        Permanent diamond = harness.addToBattlefieldAndReturn(player1, new MarbleDiamond());
        diamond.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isGreaterThanOrEqualTo(1);
        assertThat(diamond.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the mana ability while tapped")
    void cannotTapForWhiteManaWhileTapped() {
        Permanent diamond = harness.addToBattlefieldAndReturn(player1, new MarbleDiamond());
        diamond.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    private Permanent findDiamond(Player player) {
        return findPermanent(player, "Marble Diamond");
    }
}
