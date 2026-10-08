package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodedRidgeline.class})
class WoodedRidgelineTest extends BaseCardTest {

    @Test
    @DisplayName("Wooded Ridgeline enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new WoodedRidgeline()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Wooded Ridgeline").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Wooded Ridgeline taps for red mana")
    void tapsForRedMana() {
        tapFor(ManaColor.RED);
    }

    @Test
    @DisplayName("Wooded Ridgeline taps for green mana")
    void tapsForGreenMana() {
        tapFor(ManaColor.GREEN);
    }

    private void tapFor(ManaColor color) {
        Permanent ridgeline = addCreatureReady(player1, new WoodedRidgeline());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(
                color == ManaColor.RED ? ManaColor.GREEN : ManaColor.RED)).isZero();
        assertThat(ridgeline.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Wooded Ridgeline enters tapped when put onto the battlefield without being played")
    void entersTappedWithoutBeingPlayed() {
        Permanent ridgeline = harness.enterBattlefieldAndReturn(player1, new WoodedRidgeline());

        assertThat(ridgeline.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Wooded Ridgeline cannot produce mana while tapped")
    void cannotProduceManaWhileTapped() {
        harness.enterBattlefieldAndReturn(player1, new WoodedRidgeline());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A newly entered Wooded Ridgeline can produce mana after untapping")
    void newlyEnteredLandCanProduceManaAfterUntapping() {
        Permanent ridgeline = harness.enterBattlefieldAndReturn(player1, new WoodedRidgeline());
        ridgeline.untap();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(ridgeline.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
