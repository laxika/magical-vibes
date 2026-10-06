package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SelesnyaGuildgate.class})
class SelesnyaGuildgateTest extends BaseCardTest {

    @Test
    @DisplayName("Selesnya Guildgate enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new SelesnyaGuildgate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent guildgate = findPermanent(player1, "Selesnya Guildgate");
        assertThat(guildgate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between green and white")
    void activatingPromptsColorChoice() {
        addGuildgateReady(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE");
    }

    @Test
    @DisplayName("Choosing a color adds one mana of that color and taps the land")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"GREEN", "WHITE"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();
            gd = harness.getGameData();

            Permanent guildgate = addGuildgateReady(player1);
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(guildgate.isTapped()).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    private Permanent addGuildgateReady(Player player) {
        return addCreatureReady(player, new SelesnyaGuildgate());
    }

    @Test
    @DisplayName("A Guildgate put onto the battlefield enters tapped and cannot produce mana")
    void entersTappedThroughAnEffect() {
        Permanent guildgate = harness.enterBattlefieldAndReturn(player1, new SelesnyaGuildgate());

        assertThat(guildgate.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Guildgate produces mana after untapping")
    void producesManaAfterUntapping() {
        Permanent guildgate = harness.enterBattlefieldAndReturn(player1, new SelesnyaGuildgate());

        harness.performUntapStep(player1);
        assertThat(guildgate.isTapped()).isFalse();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(guildgate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly controlled untapped Guildgate can produce mana")
    void newlyControlledLandCanProduceMana() {
        Permanent guildgate = harness.addToBattlefieldAndReturn(player1, new SelesnyaGuildgate());
        guildgate.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(guildgate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

}
