package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinMatron.class, GoblinPatrol.class, Island.class})
class GoblinMatronTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may ability only offers Goblin cards")
    void acceptingMayOffersGoblins() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactly(gd.playerDecks.get(player1.getId()).getFirst());
        assertThat(search.params().reveals()).isTrue();
    }

    @Test
    @DisplayName("Choosing a Goblin puts it into the owner's hand")
    void choosingPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        Card chosenCard = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(chosenCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Accepting the may ability with no Goblin card finds nothing")
    void acceptingMayWithNoGoblinFindsNothing() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Island()));

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GoblinMatron()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new GoblinPatrol(), new Island()));
    }
}
