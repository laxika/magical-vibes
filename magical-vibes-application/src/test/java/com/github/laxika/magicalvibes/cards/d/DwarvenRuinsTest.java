package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DwarvenRuins.class)
class DwarvenRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new DwarvenRuins()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Dwarven Ruins").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Also enters tapped when placed onto the battlefield without being played")
    void entersTappedWithoutBeingPlayed() {
        Permanent ruins = harness.enterBattlefieldAndReturn(player1, new DwarvenRuins());

        assertThat(ruins.isTapped()).isTrue();
    }

    @Test
    @DisplayName("After untapping, the newly played land can immediately produce mana")
    void playedLandCanProduceManaAfterUntapping() {
        harness.setHand(player1, List.of(new DwarvenRuins()));
        harness.playLand(player1, 0);
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(findPermanent(player1, "Dwarven Ruins").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice mana resolves immediately without putting an ability on the stack")
    void sacrificeManaDoesNotUseStack() {
        harness.addToBattlefield(player1, new DwarvenRuins());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Dwarven Ruins");
        harness.assertNotOnBattlefield(player1, "Dwarven Ruins");
    }

    @Test
    @DisplayName("Tap ability adds one red mana")
    void tapAddsOneRedMana() {
        harness.addToBattlefield(player1, new DwarvenRuins());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(findPermanent(player1, "Dwarven Ruins").isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Dwarven Ruins");
    }

    @Test
    @DisplayName("Neither mana ability can be activated while the land is tapped")
    void cannotActivateManaAbilitiesWhileTapped() {
        Permanent ruins = harness.addToBattlefieldAndReturn(player1, new DwarvenRuins());
        ruins.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Tap and sacrifice adds two red mana and moves the land to the graveyard")
    void sacrificeAddsTwoRedMana() {
        harness.addToBattlefield(player1, new DwarvenRuins());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Dwarven Ruins");
        harness.assertInGraveyard(player1, "Dwarven Ruins");
    }
}
