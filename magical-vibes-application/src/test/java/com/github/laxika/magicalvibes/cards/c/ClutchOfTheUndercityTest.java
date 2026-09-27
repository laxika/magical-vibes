package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LoreBroker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClutchOfTheUndercity.class, GrayscaledGharial.class, Island.class, LoreBroker.class})
class ClutchOfTheUndercityTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentAndItsControllerLosesThreeLife() {
        harness.addToBattlefield(player2, new GrayscaledGharial());
        harness.setHand(player1, List.of(new ClutchOfTheUndercity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Grayscaled Gharial");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grayscaled Gharial");
        harness.assertInHand(player2, "Grayscaled Gharial");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void canReturnALandToItsOwnersHand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new ClutchOfTheUndercity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        ClutchOfTheUndercity clutch = new ClutchOfTheUndercity();
        ClutchOfTheUndercity matchingCard = new ClutchOfTheUndercity();
        GrayscaledGharial differentManaValue = new GrayscaledGharial();
        harness.setHand(player1, List.of(clutch));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Clutch of the Undercity");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmuteShufflesWithoutFindingAMatchingCard() {
        ClutchOfTheUndercity clutch = new ClutchOfTheUndercity();
        GrayscaledGharial nonMatchingCard = new GrayscaledGharial();
        harness.setHand(player1, List.of(clutch));
        harness.setLibrary(player1, List.of(nonMatchingCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Clutch of the Undercity");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
    }

    @Test
    void transmuteCanOnlyBeActivatedAtSorcerySpeed() {
        ClutchOfTheUndercity clutch = new ClutchOfTheUndercity();
        harness.setHand(player1, List.of(clutch));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed during your main phase");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(clutch);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void transmuteUsesItsSourceManaValueAfterAnotherCardIsDiscardedInResponse() {
        ClutchOfTheUndercity clutch = new ClutchOfTheUndercity();
        ClutchOfTheUndercity matchingCard = new ClutchOfTheUndercity();
        GrayscaledGharial drawnAndDiscardedCard = new GrayscaledGharial();
        var broker = addCreatureReady(player2, new LoreBroker());

        harness.setHand(player1, List.of(clutch));
        harness.setLibrary(player1, List.of(drawnAndDiscardedCard, matchingCard));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrayscaledGharial()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(broker), null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search)
                .as("the transmute search should still use Clutch of the Undercity's mana value after a response")
                .isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Clutch of the Undercity");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }
}
