package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Farfinder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZagothCrystal.class, Farfinder.class})
class ZagothCrystalTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one mana of a chosen Zagoth Crystal color")
    void addsChosenManaColor() {
        harness.addToBattlefield(player1, new ZagothCrystal());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling Zagoth Crystal discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ZagothCrystal()));
        harness.setLibrary(player1, List.of(new Farfinder()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Zagoth Crystal");
        harness.assertInHand(player1, "Farfinder");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLACK", "GREEN", "BLUE"})
    void producesExactlyOneManaImmediatelyAndTaps(ManaColor color) {
        var crystal = harness.addToBattlefieldAndReturn(player1, new ZagothCrystal());

        harness.activateAbility(player1, 0, null, null);
        var choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "GREEN", "BLUE");
        harness.handleListChoice(player1, color.name());

        assertThat(crystal.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void tappedCrystalCannotProduceMana() {
        var crystal = harness.addToBattlefieldAndReturn(player1, new ZagothCrystal());
        crystal.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingPaysAndDiscardsBeforeDrawing() {
        var crystal = new ZagothCrystal();
        var drawn = new Farfinder();
        harness.setHand(player1, List.of(crystal));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(crystal);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(crystal);
    }

    @Test
    void cyclingCannotDiscardWithoutEnoughMana() {
        var crystal = new ZagothCrystal();
        harness.setHand(player1, List.of(crystal));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(crystal);
        harness.assertNotInGraveyard(player1, "Zagoth Crystal");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
