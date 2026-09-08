package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.s.SteelHellkite;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrophyMageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Trophy Mage creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        resolveEnterTheBattlefieldTrigger();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may ability offers only artifacts with mana value 3")
    void acceptingMayPresentsOnlyManaValueThreeArtifacts() {
        setupAndCast();
        setupLibrary();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName()).containsExactly("Manalith");
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing an artifact puts it into hand")
    void choosingArtifactPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, true);

        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(
                harness.getGameData(), player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Manalith");
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNull();
        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Failing to find is allowed")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.getGameService().handleInteractionAnswer(
                harness.getGameData(), player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new TrophyMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTheBattlefieldTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setupLibrary() {
        List<com.github.laxika.magicalvibes.model.Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Manalith(), new GolemsHeart(), new SteelHellkite(), new GrizzlyBears()));
    }
}
