package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempleOfMalice.class})
class TempleOfMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Temple of Malice enters the battlefield tapped")
    void entersBattlefieldTapped() {
        playTempleOfMalice(player1);

        assertThat(findTemple(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving Temple of Malice's enter trigger opens a scry 1 interaction")
    void enterTriggerScryOne() {
        playTempleOfMalice(player1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Scrying to the bottom moves the top card to the bottom of the library")
    void scryToBottom() {
        playTempleOfMalice(player1);
        GameData gameData = harness.getGameData();
        List<Card> deck = gameData.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gameData,
                player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0))
        );

        assertThat(deck.getLast()).isSameAs(originalTop);
    }

    @Test
    @DisplayName("Temple of Malice produces the chosen black or red mana")
    void producesChosenMana() {
        for (ManaColor color : List.of(ManaColor.BLACK, ManaColor.RED)) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent temple = addTempleReady(player1);
            GameData gameData = harness.getGameData();

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(gameData.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(temple.isTapped()).isTrue();
            assertThat(gameData.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Temple of Malice's controller may keep the top card without changing either library")
    void controllerKeepsTopCard() {
        Card top = new TempleOfMalice();
        Card second = new TempleOfMalice();
        Card opponentTop = new TempleOfMalice();
        harness.setLibrary(player2, List.of(top, second));
        harness.setLibrary(player1, List.of(opponentTop));
        playTempleOfMalice(player2);

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(top);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Temple of Malice's scry resolves with an empty library without asking for a choice")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        playTempleOfMalice(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findTemple(player1).isTapped()).isTrue();
    }

    private void playTempleOfMalice(Player player) {
        harness.setHand(player, List.of(new TempleOfMalice()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }

    private Permanent addTempleReady(Player player) {
        Permanent temple = harness.addToBattlefieldAndReturn(player, new TempleOfMalice());
        temple.setSummoningSick(false);
        return temple;
    }

    private Permanent findTemple(Player player) {
        return findPermanent(player, "Temple of Malice");
    }
}
