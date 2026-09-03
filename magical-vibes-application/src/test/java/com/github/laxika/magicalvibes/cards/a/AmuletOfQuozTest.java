package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmuletOfQuoz.class})
class AmuletOfQuozTest extends BaseCardTest {

    /** Puts the Amulet onto player1's battlefield and moves the game to player1's upkeep. */
    private Permanent amuletInUpkeep() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new AmuletOfQuoz());
        advanceToUpkeep(player1);
        return amulet;
    }

    @Test
    @DisplayName("Anteing the top card of the library removes it from the game and no coin is flipped")
    void anteingExilesTopCardAndSkipsFlip() {
        Permanent amulet = amuletInUpkeep();
        AmuletOfQuoz topCard = new AmuletOfQuoz();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities(); // resolve ability -> ante prompt for player2

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(gd.antedCardIds).contains(topCard.getId());
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(l -> l.contains("coin flip"));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(amulet);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(amulet.getCard());
    }

    @Test
    @DisplayName("Declining to ante flips a coin and exactly one of the two players loses the game")
    void decliningFlipsCoinAndSomeoneLoses() {
        amuletInUpkeep();
        AmuletOfQuoz topCard = new AmuletOfQuoz();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        // The library is untouched — declining antes nothing.
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        assertThat(logs).anyMatch(l -> l.contains("coin flip for Amulet of Quoz"));
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);

        boolean controllerWonFlip = logs.stream().anyMatch(l -> l.contains(player1.getUsername() + " wins the coin flip"));
        String expectedLoser = controllerWonFlip ? player2.getUsername() : player1.getUsername();
        String expectedWinner = controllerWonFlip ? player1.getUsername() : player2.getUsername();
        assertThat(logs).anyMatch(l -> l.contains(expectedLoser + " loses the game from Amulet of Quoz"));
        assertThat(gd.playerIdToName.get(gd.winnerPlayerId)).isEqualTo(expectedWinner);
    }

    @Test
    @DisplayName("An opponent with an empty library can't ante, so the coin is flipped with no prompt")
    void emptyLibraryFlipsImmediately() {
        amuletInUpkeep();
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(l -> l.contains("coin flip for Amulet of Quoz"));
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The ability cannot be activated outside your upkeep")
    void cannotActivateOutsideUpkeep() {
        harness.addToBattlefield(player1, new AmuletOfQuoz());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("The ability cannot be activated during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AmuletOfQuoz());
        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your upkeep");
    }

    @Test
    @DisplayName("The ability cannot be activated with a tapped Amulet")
    void cannotActivateTappedAmulet() {
        Permanent amulet = amuletInUpkeep();
        amulet.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The ability cannot target the activating player")
    void cannotTargetSelf() {
        amuletInUpkeep();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
