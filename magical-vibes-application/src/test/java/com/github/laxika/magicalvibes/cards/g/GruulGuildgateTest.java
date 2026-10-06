package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GruulGuildgate.class})
class GruulGuildgateTest extends BaseCardTest {

    @Test
    @DisplayName("Gruul Guildgate enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new GruulGuildgate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent guildgate = findPermanent(player1, "Gruul Guildgate");
        assertThat(guildgate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between red and green")
    void activatingPromptsColorChoice() {
        addCreatureReady(player1, new GruulGuildgate());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "GREEN");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"RED", "GREEN"})
    @DisplayName("Choosing a color adds one mana of that color and taps the land")
    void choosingColorAddsThatMana(ManaColor manaColor) {
        Permanent guildgate = addCreatureReady(player1, new GruulGuildgate());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, manaColor.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(
                manaColor == ManaColor.RED ? ManaColor.GREEN : ManaColor.RED)).isZero();
        assertThat(guildgate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gruul Guildgate also enters tapped when put onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent guildgate = harness.enterBattlefieldAndReturn(player1, new GruulGuildgate());

        assertThat(guildgate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Gruul Guildgate cannot activate its mana ability")
    void cannotActivateWhileTapped() {
        Permanent guildgate = addCreatureReady(player1, new GruulGuildgate());
        guildgate.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}