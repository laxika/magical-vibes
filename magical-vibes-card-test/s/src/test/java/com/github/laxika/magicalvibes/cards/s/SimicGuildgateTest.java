package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SimicGuildgate.class})
class SimicGuildgateTest extends BaseCardTest {

    @Test
    @DisplayName("Simic Guildgate enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new SimicGuildgate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent guildgate = findPermanent(player1, "Simic Guildgate");
        assertThat(guildgate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between green and blue")
    void activatingPromptsColorChoice() {
        addCreatureReady(player1, new SimicGuildgate());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "BLUE");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "BLUE"})
    @DisplayName("Choosing a color adds one mana of that color and taps the land")
    void choosingColorAddsThatMana(ManaColor manaColor) {
        Permanent guildgate = addCreatureReady(player1, new SimicGuildgate());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, manaColor.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(guildgate.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Simic Guildgate cannot activate its mana ability")
    void cannotActivateWhileTapped() {
        Permanent guildgate = harness.enterBattlefieldAndReturn(player1, new SimicGuildgate());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(guildgate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simic Guildgate put onto the battlefield enters tapped and can produce mana if untapped that turn")
    void canProduceManaOnEntryTurnAfterUntapping() {
        Permanent guildgate = harness.enterBattlefieldAndReturn(player1, new SimicGuildgate());
        assertThat(guildgate.isTapped()).isTrue();
        guildgate.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(guildgate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
