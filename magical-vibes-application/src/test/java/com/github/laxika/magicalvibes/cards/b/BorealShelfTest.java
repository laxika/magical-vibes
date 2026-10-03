package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorealShelf.class})
class BorealShelfTest extends BaseCardTest {

    @Test
    void entersTappedWhenPutOntoBattlefield() {
        Permanent shelf = harness.enterBattlefieldAndReturn(player1, new BorealShelf());

        assertThat(shelf.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileTappedAfterEntering() {
        harness.setHand(player1, List.of(new BorealShelf()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void canProduceManaAfterUntapping() {
        harness.setHand(player1, List.of(new BorealShelf()));
        harness.playLand(player1, 0);
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE"})
    void producedManaRetainsSnowSource(ManaColor color) {
        Permanent shelf = harness.addToBattlefieldAndReturn(player1, new BorealShelf());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE");
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(color)).isEqualTo(1);
        assertThat(shelf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boreal Shelf enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new BorealShelf()));
        harness.playLand(player1, 0);

        Permanent shelf = findPermanent(player1, "Boreal Shelf");

        assertThat(shelf.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE"})
    @DisplayName("Boreal Shelf adds the chosen white or blue mana")
    void addsChosenMana(ManaColor manaColor) {
        Permanent shelf = harness.addToBattlefieldAndReturn(player1, new BorealShelf());
        shelf.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player1, manaColor.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(shelf.isTapped()).isTrue();
    }
}
