package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({MirageMesa.class})
class MirageMesaTest extends BaseCardTest {

    @Test
    @DisplayName("Mirage Mesa enters tapped and lets its controller choose a color")
    void entersTappedAndChoosesColor() {
        harness.setHand(player1, List.of(new MirageMesa()));
        harness.playLand(player1, 0);

        Permanent mesa = findPermanent(player1, "Mirage Mesa");
        assertThat(mesa.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(mesa.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Mirage Mesa taps for one mana of its chosen color")
    void tapsForChosenColor() {
        Permanent mesa = harness.addToBattlefieldAndReturn(player1, new MirageMesa());
        mesa.setSummoningSick(false);
        mesa.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(mesa.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("Each color chosen on entry determines the mana produced without another choice")
    void producesColorChosenOnEntry(CardColor color) {
        harness.setHand(player1, List.of(new MirageMesa()));
        harness.playLand(player1, 0);

        PendingInteraction.ColorChoice choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, color.name());
        Permanent mesa = findPermanent(player1, "Mirage Mesa");
        assertThat(mesa.isTapped()).isTrue();
        mesa.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor.name().equals(color.name()) ? 1 : 0);
        }
        assertThat(mesa.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copies entering without being played retain independent chosen colors")
    void copiesChooseColorsIndependently() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new MirageMesa());
        harness.handleListChoice(player1, "WHITE");
        Permanent second = harness.enterBattlefieldAndReturn(player1, new MirageMesa());
        harness.handleListChoice(player1, "GREEN");

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        first.untap();
        second.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
