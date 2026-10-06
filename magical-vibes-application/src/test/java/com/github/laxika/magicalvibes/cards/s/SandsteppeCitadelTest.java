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

@CardUsed({SandsteppeCitadel.class})
class SandsteppeCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new SandsteppeCitadel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Sandsteppe Citadel").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between white, black, and green")
    void activatingPromptsColorChoice() {
        addCitadelReady(player1);
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gameData.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLACK", "GREEN");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color and taps the land")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"WHITE", "BLACK", "GREEN"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent citadel = addCitadelReady(player1);
            GameData gameData = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gameData.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(citadel.isTapped()).isTrue();
            assertThat(gameData.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield without being played")
    void entersTappedWithoutBeingPlayed() {
        Permanent citadel = harness.enterBattlefieldAndReturn(player1, new SandsteppeCitadel());

        assertThat(citadel.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot produce mana on entry, but can after untapping on the same turn")
    void producesManaAfterUntapping() {
        harness.setHand(player1, List.of(new SandsteppeCitadel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        findPermanent(player1, "Sandsteppe Citadel").untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private Permanent addCitadelReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SandsteppeCitadel());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
