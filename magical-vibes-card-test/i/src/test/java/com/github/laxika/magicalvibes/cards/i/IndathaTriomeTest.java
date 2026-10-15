package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MosscoatGoriak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IndathaTriome.class, MosscoatGoriak.class})
class IndathaTriomeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new IndathaTriome()));

        harness.playLand(player1, 0);

        Permanent triome = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(triome.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"WHITE", "BLACK", "GREEN"})
    @DisplayName("Mana ability adds the chosen color")
    void addsChosenManaColor(String color) {
        Permanent triome = harness.addToBattlefieldAndReturn(player1, new IndathaTriome());
        triome.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        ManaColor manaColor = ManaColor.valueOf(color);
        harness.handleListChoice(player1, color);

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(triome.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new IndathaTriome()));
        harness.setLibrary(player1, List.of(new MosscoatGoriak()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Indatha Triome");
        harness.assertInHand(player1, "Mosscoat Goriak");
    }

    @Test
    @DisplayName("Cycling pays mana and discards before its draw resolves")
    void cyclingPaysCostsBeforeDrawing() {
        harness.setHand(player1, List.of(new IndathaTriome()));
        harness.setLibrary(player1, List.of(new MosscoatGoriak()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Indatha Triome");
        harness.assertNotInHand(player1, "Indatha Triome");
        harness.assertNotInHand(player1, "Mosscoat Goriak");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Mosscoat Goriak");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only two mana")
    void cyclingRequiresThreeMana() {
        harness.setHand(player1, List.of(new IndathaTriome()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Indatha Triome");
        harness.assertNotInGraveyard(player1, "Indatha Triome");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Triome cannot produce mana")
    void cannotTapWhileTapped() {
        harness.setHand(player1, List.of(new IndathaTriome()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLACK, ManaColor.GREEN)) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }
}
