package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RavagedHighlands.class)
class RavagedHighlandsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RavagedHighlands()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Ravaged Highlands").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one red mana")
    void tapAddsRedMana() {
        harness.addToBattlefield(player1, new RavagedHighlands());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ravaged Highlands");
    }

    @Test
    @DisplayName("Neither mana ability can be activated while the land is tapped")
    void cannotActivateManaAbilitiesWhileTapped() {
        Permanent highlands = harness.addToBattlefieldAndReturn(player1, new RavagedHighlands());
        highlands.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Sacrifice ability adds mana of the chosen color and moves the land to the graveyard")
    void sacrificeAddsChosenColorMana() {
        harness.addToBattlefield(player1, new RavagedHighlands());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Ravaged Highlands");
        harness.assertInGraveyard(player1, "Ravaged Highlands");
    }

    @Test
    @DisplayName("Sacrifice ability can add blue mana when blue is chosen")
    void sacrificeAddsBlueManaWhenChosen() {
        harness.addToBattlefield(player1, new RavagedHighlands());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertNotOnBattlefield(player1, "Ravaged Highlands");
        harness.assertInGraveyard(player1, "Ravaged Highlands");
    }
}
