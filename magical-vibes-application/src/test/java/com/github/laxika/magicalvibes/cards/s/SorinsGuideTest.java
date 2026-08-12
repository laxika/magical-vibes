package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SorinsGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sorin's Guide triggers a may ability prompt")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may finds Sorin, Vampire Lord in the graveyard")
    void acceptingMayFindsInGraveyard() {
        Card sorin = createSorinVampireLord();
        harness.setGraveyard(player1, List.of(sorin));
        setupAndCast();

        resolveMayPrompt(true);

        harness.assertInHand(player1, "Sorin, Vampire Lord");
        harness.assertNotInGraveyard(player1, "Sorin, Vampire Lord");
    }

    @Test
    @DisplayName("Accepting may searches the library when Sorin is not in the graveyard")
    void acceptingMaySearchesLibrary() {
        Card sorin = createSorinVampireLord();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(sorin);
        setupAndCast();

        resolveMayPrompt(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Sorin, Vampire Lord");
    }

    @Test
    @DisplayName("Declining may leaves Sorin in the graveyard")
    void decliningMayDoesNotSearch() {
        Card sorin = createSorinVampireLord();
        harness.setGraveyard(player1, List.of(sorin));
        setupAndCast();

        resolveMayPrompt(false);

        harness.assertInGraveyard(player1, "Sorin, Vampire Lord");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting may when Sorin is absent from both zones finds nothing")
    void acceptingMayWhenNotFound() {
        gd.playerDecks.get(player1.getId()).clear();
        setupAndCast();

        resolveMayPrompt(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new SorinsGuide()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveMayPrompt(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private Card createSorinVampireLord() {
        Card sorin = new Card();
        sorin.setName("Sorin, Vampire Lord");
        sorin.setType(CardType.PLANESWALKER);
        sorin.setManaCost("{3}{B}{B}");
        return sorin;
    }
}
