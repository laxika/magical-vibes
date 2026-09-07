package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({KetriaTriome.class, GrizzlyBears.class})
class KetriaTriomeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new KetriaTriome()));

        harness.playLand(player1, 0);

        Permanent triome = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(triome.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GREEN", "BLUE", "RED"})
    @DisplayName("Mana ability adds the chosen color")
    void addsChosenManaColor(String color) {
        Permanent triome = new Permanent(new KetriaTriome());
        triome.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(triome);

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
        harness.setHand(player1, List.of(new KetriaTriome()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ketria Triome");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
