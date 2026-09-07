package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SplintersTechnique.class, GrizzlyBears.class})
class SplintersTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the library for any card when cast normally")
    void searchesLibraryWhenCastNormally() {
        Card technique = new SplintersTechnique();
        Card chosenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(technique));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        setLibrary(chosenCard);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        chooseLibraryCard(chosenCard);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Splinter's Technique");
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker before searching the library")
    void sneakReturnsUnblockedAttackerAndSearches() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Card chosenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new SplintersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setLibrary(chosenCard);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        chooseLibraryCard(chosenCard);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SplintersTechnique);
    }

    private void chooseLibraryCard(Card chosenCard) {
        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();

        int chosenIndex = search.params().cards().indexOf(chosenCard);
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(chosenIndex));
    }

    private void setLibrary(Card... cards) {
        List<Card> library = harness.getGameData().playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(cards));
    }
}
