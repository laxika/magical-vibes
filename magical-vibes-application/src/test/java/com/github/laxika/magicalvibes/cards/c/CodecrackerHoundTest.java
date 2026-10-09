package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CodecrackerHound.class})
class CodecrackerHoundTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts one of the top two cards into hand and the other into the graveyard")
    void choosesOneCardForHandAndPutsTheOtherInGraveyard() {
        Card chosen = new CodecrackerHound();
        Card other = new CodecrackerHound();
        harness.setLibrary(player1, List.of(chosen, other));
        castCodecrackerHound();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gameData.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(other);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Warp exiles Codecracker Hound at the next end step")
    void warpExilesTheCreatureAtTheNextEndStep() {
        CodecrackerHound hound = new CodecrackerHound();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(hound));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(hound.getId())).isNotNull();
    }

    private void castCodecrackerHound() {
        harness.castFromHand(player1, new CodecrackerHound(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void mustChooseOneCardWhenTwoAreAvailable() {
        Card first = new CodecrackerHound();
        Card second = new CodecrackerHound();
        harness.setLibrary(player1, List.of(first, second));
        castCodecrackerHound();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
    }

    @Test
    void singleRemainingLibraryCardGoesIntoHand() {
        Card remaining = new CodecrackerHound();
        harness.setLibrary(player1, List.of(remaining));
        castCodecrackerHound();

        assertThat(gd.playerHands.get(player1.getId())).contains(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotPreventEnteringTheBattlefield() {
        harness.setLibrary(player1, List.of());
        castCodecrackerHound();

        harness.assertOnBattlefield(player1, "Codecracker Hound");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void normalCastDoesNotExileAtEndStep() {
        harness.setLibrary(player1, List.of());
        castCodecrackerHound();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Codecracker Hound");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void warpExileUsesTheStackAndAllowsResponses() {
        CodecrackerHound hound = new CodecrackerHound();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(hound));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Codecracker Hound");
        assertThat(gd.findExiledCard(hound.getId())).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.findExiledCard(hound.getId())).isNotNull();
    }

    @Test
    void warpedCardCanBeCastOnALaterTurnAndStaysOnTheBattlefield() {
        harness.setHand(player2, List.of());
        CodecrackerHound hound = new CodecrackerHound();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(hound));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(hound.getId())).isNotNull();

        harness.setLibrary(player1, List.of(new CodecrackerHound()));
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, hound.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Codecracker Hound");
        assertThat(gd.findExiledCard(hound.getId())).isNull();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(hound.getId()));
    }
}
