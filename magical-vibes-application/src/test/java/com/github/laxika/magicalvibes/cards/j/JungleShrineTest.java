package com.github.laxika.magicalvibes.cards.j;

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

@CardUsed({JungleShrine.class})
class JungleShrineTest extends BaseCardTest {

    @Test
    @DisplayName("Jungle Shrine enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new JungleShrine()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent shrine = findPermanent(player1, "Jungle Shrine");
        assertThat(shrine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between red, green, and white")
    void activatingPromptsColorChoice() {
        addShrineReady(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "GREEN", "WHITE");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color and taps the land")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"RED", "GREEN", "WHITE"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent shrine = addShrineReady(player1);
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(shrine.isTapped()).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Jungle Shrine enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent shrine = harness.enterBattlefieldAndReturn(player1, new JungleShrine());

        assertThat(shrine.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Jungle Shrine cannot produce mana again")
    void cannotActivateWhileTapped() {
        addShrineReady(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An untapped noncreature Jungle Shrine can produce mana the turn it enters")
    void canActivateWithoutClearingSummoningSickness() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new JungleShrine());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(shrine.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addShrineReady(Player player) {
        return addCreatureReady(player, new JungleShrine());
    }
}
