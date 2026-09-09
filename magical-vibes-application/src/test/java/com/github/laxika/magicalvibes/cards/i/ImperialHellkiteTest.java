package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImperialHellkite.class, GrizzlyBears.class, Island.class})
class ImperialHellkiteTest extends BaseCardTest {

    @Test
    void turningFaceUpCreatesOptionalDragonSearch() {
        Permanent hellkite = castFaceDown();
        setupLibrary();

        turnFaceUp(hellkite);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void acceptingSearchOffersOnlyDragonCardsAndPutsChosenCardIntoHand() {
        Permanent hellkite = castFaceDown();
        Card dragon = new ImperialHellkite();
        Card nonDragon = new GrizzlyBears();
        Card land = new Island();
        harness.setLibrary(player1, List.of(dragon, nonDragon, land));

        turnFaceUp(hellkite);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(1)
                .allMatch(card -> card.getSubtypes().contains(CardSubtype.DRAGON));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dragon.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(dragon.getId()));
    }

    @Test
    void decliningSearchLeavesLibraryUnchanged() {
        Permanent hellkite = castFaceDown();
        setupLibrary();
        List<Card> library = new ArrayList<>(gd.playerDecks.get(player1.getId()));

        turnFaceUp(hellkite);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    private Permanent castFaceDown() {
        ImperialHellkite card = new ImperialHellkite();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void turnFaceUp(Permanent hellkite) {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hellkite));
        harness.passBothPriorities();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new ImperialHellkite(), new GrizzlyBears(), new Island()));
    }
}
