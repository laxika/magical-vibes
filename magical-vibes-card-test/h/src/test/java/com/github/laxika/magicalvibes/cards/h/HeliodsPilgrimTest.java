package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DivineFavor;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeliodsPilgrim.class, DivineFavor.class, RuneclawBear.class})
class HeliodsPilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers Aura cards from the library")
    void acceptingEtbAbilityOffersAuras() {
        Card aura = new DivineFavor();
        setupAndCast(List.of(aura, new RuneclawBear()));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(aura);
    }

    @Test
    @DisplayName("Choosing an Aura card puts it into hand")
    void choosingAuraPutsItIntoHand() {
        Card aura = new DivineFavor();
        setupAndCast(List.of(aura));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability skips the library search")
    void decliningEtbAbilitySkipsSearch() {
        setupAndCast(List.of(new DivineFavor()));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The chosen Aura is revealed and removed from the library without entering")
    void chosenAuraIsRevealedWithoutEntering() {
        Card firstAura = new DivineFavor();
        Card secondAura = new DivineFavor();
        Card bear = new RuneclawBear();
        setupAndCast(List.of(firstAura, bear, secondAura));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondAura);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(firstAura, bear);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gameLogContains("reveals Divine Favor")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find even when an Aura is available")
    void mayFailToFindAvailableAura() {
        Card aura = new DivineFavor();
        setupAndCast(List.of(aura));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting with no Aura still finishes the search and shuffles")
    void noMatchingAuraFinishesSearch() {
        Card bear = new RuneclawBear();
        setupAndCast(List.of(bear));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting with an empty library completes normally")
    void emptyLibraryFinishesSearch() {
        setupAndCast(List.of());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    private void setupAndCast(List<Card> library) {
        harness.setHand(player1, List.of(new HeliodsPilgrim()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

}
