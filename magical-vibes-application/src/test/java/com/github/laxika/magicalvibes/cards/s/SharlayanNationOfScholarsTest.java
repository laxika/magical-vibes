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
        addCreatureReady(player1, new SharlayanNationOfScholars());
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gameData.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE"})
    @DisplayName("Choosing either color adds one mana and taps the land")
    void choosingColorAddsMana(ManaColor manaColor) {
        Permanent sharlayan = addCreatureReady(player1, new SharlayanNationOfScholars());
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, manaColor.name());

        assertThat(gameData.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(sharlayan.isTapped()).isTrue();
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    void entersTappedWhenPutOntoBattlefieldWithoutBeingPlayed() {
        Permanent sharlayan = harness.enterBattlefieldAndReturn(player1, new SharlayanNationOfScholars());

        assertThat(sharlayan.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent sharlayan = harness.enterBattlefieldAndReturn(player1, new SharlayanNationOfScholars());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(sharlayan.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void canActivateOnTurnItEntersAfterBeingUntapped() {
        Permanent sharlayan = harness.enterBattlefieldAndReturn(player1, new SharlayanNationOfScholars());
        sharlayan.setTapped(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(sharlayan.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
