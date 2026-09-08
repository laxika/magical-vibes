package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoldmaneGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Goldmane Griffin triggers a may ability")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability returns Ajani, Inspiring Leader from the graveyard")
    void acceptingMayFindsAjaniInGraveyard() {
        harness.setGraveyard(player1, List.of(createAjani()));
        setupAndCast();

        resolveMay(true);

        harness.assertInHand(player1, "Ajani, Inspiring Leader");
        harness.assertNotInGraveyard(player1, "Ajani, Inspiring Leader");
    }

    @Test
    @DisplayName("Accepting the may ability searches the library when Ajani is not in the graveyard")
    void acceptingMaySearchesLibrary() {
        Card ajani = createAjani();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(ajani);
        setupAndCast();

        resolveMay(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName()).isEqualTo("Ajani, Inspiring Leader");
    }

    @Test
    @DisplayName("Declining the may ability leaves Ajani in the graveyard")
    void decliningMayDoesNotSearch() {
        harness.setGraveyard(player1, List.of(createAjani()));
        setupAndCast();

        resolveMay(false);

        harness.assertInGraveyard(player1, "Ajani, Inspiring Leader");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Goldmane Griffin enters the battlefield")
    void goldmaneGriffinEntersBattlefield() {
        setupAndCast();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goldmane Griffin");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GoldmaneGriffin()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveMay(boolean choice) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private Card createAjani() {
        Card ajani = new Card();
        ajani.setName("Ajani, Inspiring Leader");
        ajani.setType(CardType.PLANESWALKER);
        ajani.setManaCost("{3}{W}{W}");
        return ajani;
    }
}
