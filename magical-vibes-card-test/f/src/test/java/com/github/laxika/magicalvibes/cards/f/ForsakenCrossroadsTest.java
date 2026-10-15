package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForsakenCrossroads.class})
class ForsakenCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("The starting player's Crossroads stays tapped after scrying")
    void startingPlayerCannotUntapIt() {
        gd.startingPlayerId = player1.getId();
        playLand(player1);

        Permanent crossroads = findPermanent(player1, "Forsaken Crossroads");
        assertThat(crossroads.isTapped()).isTrue();
        chooseColor(player1);
        resolveScry(player1);

        assertThat(crossroads.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A non-starting player may untap Crossroads instead of scrying")
    void nonStartingPlayerMayUntapIt() {
        gd.startingPlayerId = player1.getId();
        playLand(player2);

        Permanent crossroads = findPermanent(player2, "Forsaken Crossroads");
        chooseColor(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(crossroads.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining to untap scries one and leaves Crossroads tapped")
    void nonStartingPlayerMayChooseScryInstead() {
        gd.startingPlayerId = player1.getId();
        ForsakenCrossroads top = new ForsakenCrossroads();
        ForsakenCrossroads second = new ForsakenCrossroads();
        harness.setLibrary(player2, List.of(top, second));
        playLand(player2);
        Permanent crossroads = findPermanent(player2, "Forsaken Crossroads");
        chooseColor(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, top);
        assertThat(crossroads.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The starting player can put the top card on the bottom")
    void startingPlayerScriesOneToBottom() {
        gd.startingPlayerId = player1.getId();
        ForsakenCrossroads top = new ForsakenCrossroads();
        ForsakenCrossroads second = new ForsakenCrossroads();
        harness.setLibrary(player1, List.of(top, second));
        playLand(player1);
        chooseColor(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(findPermanent(player1, "Forsaken Crossroads").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A non-starting player can untap Crossroads with an empty library")
    void canUntapWithEmptyLibrary() {
        gd.startingPlayerId = player1.getId();
        harness.setLibrary(player2, List.of());
        playLand(player2);
        chooseColor(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanent(player2, "Forsaken Crossroads").isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Tapping Crossroads adds its chosen color")
    void tapsForChosenColor() {
        Permanent crossroads = harness.addToBattlefieldAndReturn(player1, new ForsakenCrossroads());
        crossroads.setSummoningSick(false);
        crossroads.setChosenColor(CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The entry color choice determines the mana produced")
    void producesColorChosenOnEntry(CardColor color) {
        gd.startingPlayerId = player1.getId();
        playLand(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, color.name());
        resolveScry(player1);
        harness.performUntapStep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color.name()))).isEqualTo(1);
        assertThat(findPermanent(player1, "Forsaken Crossroads").isTapped()).isTrue();
    }

    private void playLand(Player player) {
        harness.setHand(player, List.of(new ForsakenCrossroads()));
        var startingPlayerId = gd.startingPlayerId;
        harness.forceActivePlayer(player);
        gd.startingPlayerId = startingPlayerId;
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }

    private void chooseColor(Player player) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player, CardColor.BLUE.name());
    }

    private void resolveScry(Player player) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
