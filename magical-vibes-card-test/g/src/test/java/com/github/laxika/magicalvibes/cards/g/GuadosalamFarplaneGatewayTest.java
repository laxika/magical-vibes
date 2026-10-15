package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GuadosalamFarplaneGateway.class)
class GuadosalamFarplaneGatewayTest extends BaseCardTest {

    @Test
    @DisplayName("Guadosalam, Farplane Gateway enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new GuadosalamFarplaneGateway()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent gateway = findPermanent(player1, "Guadosalam, Farplane Gateway");
        assertThat(gateway.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between green and blue")
    void activatingPromptsColorChoice() {
        addGatewayReady(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "BLUE");
    }

    @Test
    @DisplayName("Choosing a color adds one mana of that color and taps the land")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"GREEN", "BLUE"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            gd = harness.getGameData();
            harness.skipMulligan();

            Permanent gateway = addGatewayReady(player1);
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(gateway.isTapped()).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    private Permanent addGatewayReady(Player player) {
        return addCreatureReady(player, new GuadosalamFarplaneGateway());
    }

    @Test
    @DisplayName("A newly played tapped Gateway cannot produce mana")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new GuadosalamFarplaneGateway()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gateway can produce mana after untapping and cannot tap twice")
    void producesManaAfterUntappingButCannotActivateTwice() {
        harness.setHand(player1, List.of(new GuadosalamFarplaneGateway()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }
}
