package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AltarOfTheBrood.class, Forest.class})
class AltarOfTheBroodTest extends BaseCardTest {

    @Test
    @DisplayName("Another permanent entering under the controller's control makes each opponent mill one")
    void ownPermanentEnteringMillsOpponent() {
        harness.addToBattlefield(player1, new AltarOfTheBrood());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A permanent entering under an opponent's control does not trigger it")
    void opponentPermanentEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new AltarOfTheBrood());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Altar of the Brood does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, new AltarOfTheBrood(), "{1}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A second Altar triggers the first Altar but not itself")
    void anotherAltarEnteringTriggersOnlyExistingAltar() {
        harness.addToBattlefield(player1, new AltarOfTheBrood());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.castFromHand(player1, new AltarOfTheBrood(), "{1}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only the opponent's top card is milled, after the trigger resolves")
    void millsTopCardWithoutMillingController() {
        harness.addToBattlefield(player1, new AltarOfTheBrood());
        Forest controllerCard = new Forest();
        Forest topCard = new Forest();
        Forest bottomCard = new Forest();
        harness.setLibrary(player1, List.of(controllerCard));
        harness.setLibrary(player2, List.of(topCard, bottomCard));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, bottomCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bottomCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling an empty library does not make the opponent lose")
    void emptyLibraryDoesNotCauseLoss() {
        harness.addToBattlefield(player1, new AltarOfTheBrood());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
