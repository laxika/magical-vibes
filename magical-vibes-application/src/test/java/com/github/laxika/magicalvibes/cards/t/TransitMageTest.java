package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CitanulFlute;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransitMageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Transit Mage creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability offers only artifact cards with mana value 4 or 5")
    void acceptingMayOffersMatchingArtifacts() {
        setupAndCast();
        CitanulFlute flute = new CitanulFlute();
        SolemnSimulacrum simulacrum = new SolemnSimulacrum();
        setupLibrary(flute, simulacrum, new MindStone(), new WurmcoilEngine(), new GrizzlyBears());

        resolveMayAbility(true);

        PendingInteraction.LibrarySearch search =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(flute, simulacrum);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a matching artifact puts it into hand")
    void choosingArtifactPutsItIntoHand() {
        setupAndCast();
        CitanulFlute flute = new CitanulFlute();
        SolemnSimulacrum simulacrum = new SolemnSimulacrum();
        setupLibrary(flute, simulacrum, new MindStone(), new WurmcoilEngine(), new GrizzlyBears());
        resolveMayAbility(true);

        GameData gd = harness.getGameData();
        int chosenIndex = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().indexOf(flute);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(chosenIndex));

        assertThat(gd.playerHands.get(player1.getId())).contains(flute);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(flute);
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary(new CitanulFlute());

        resolveMayAbility(false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Cards outside the artifact and mana value range cannot be found")
    void cardsOutsideRangeAreExcluded() {
        setupAndCast();
        setupLibrary(new MindStone(), new WurmcoilEngine(), new GrizzlyBears());

        resolveMayAbility(true);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Transit Mage can fail to find a card")
    void canFailToFind() {
        setupAndCast();
        CitanulFlute flute = new CitanulFlute();
        setupLibrary(flute);
        resolveMayAbility(true);

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(flute);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new TransitMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }

    private void resolveMayAbility(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
