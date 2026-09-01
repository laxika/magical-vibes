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

@CardUsed(SharlayanNationOfScholars.class)
class SharlayanNationOfScholarsTest extends BaseCardTest {

    @Test
    @DisplayName("Sharlayan, Nation of Scholars enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new SharlayanNationOfScholars()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Sharlayan, Nation of Scholars").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between white and blue")
    void activatingPromptsColorChoice() {
        addSharlayanReady(player1);
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gameData.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE");
    }

    @Test
    @DisplayName("Choosing either color adds one mana and taps the land")
    void choosingColorAddsMana() {
        for (String color : new String[]{"WHITE", "BLUE"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent sharlayan = addSharlayanReady(player1);
            GameData gameData = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gameData.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(sharlayan.isTapped()).isTrue();
            assertThat(gameData.interaction.activeInteraction()).isNull();
        }
    }

    private Permanent addSharlayanReady(Player player) {
        Permanent permanent = new Permanent(new SharlayanNationOfScholars());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
