package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CavalierOfThornsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a chosen land onto the battlefield and the rest into the graveyard")
    void etbPutsLandOntoBattlefieldAndRestIntoGraveyard() {
        Forest forest = new Forest();
        Forest mountain = new Forest();
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        Shock shock3 = new Shock();
        setLibrary(shock1, forest, mountain, shock2, shock3);

        CavalierOfThorns cavalier = new CavalierOfThorns();
        castAndResolve(cavalier);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest, mountain);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().restToGraveyard()).isTrue();
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(shock1, mountain, shock2, shock3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB puts all five cards into the graveyard when no land is revealed")
    void etbPutsAllCardsIntoGraveyardWithoutLand() {
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        Shock shock3 = new Shock();
        Shock shock4 = new Shock();
        Shock shock5 = new Shock();
        setLibrary(shock1, shock2, shock3, shock4, shock5);

        castAndResolve(new CavalierOfThorns());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(shock1, shock2, shock3, shock4, shock5);
    }

    @Test
    @DisplayName("Death trigger may exile Cavalier and excludes it from the target choices")
    void deathTriggerExilesSourceAndReturnsAnotherCardToLibraryTop() {
        CavalierOfThorns cavalier = new CavalierOfThorns();
        Card target1 = new Shock();
        Card target2 = new Forest();
        addCreatureReady(player1, cavalier);
        harness.setGraveyard(player1, List.of(target1, target2));
        castWrathOfGod();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(target1.getId()).doesNotContain(cavalier.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target1.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(cavalier.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(target1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target2);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private void castAndResolve(Card card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castWrathOfGod() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
