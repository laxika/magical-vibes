package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cessation;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SanctumOfAll;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShrineSteward.class, Cessation.class, Forest.class, SanctumOfAll.class})
class ShrineStewardTest extends BaseCardTest {

    @Test
    @DisplayName("Offers Aura and Shrine cards from the library")
    void offersAuraAndShrineCards() {
        Cessation aura = new Cessation();
        SanctumOfAll shrine = new SanctumOfAll();
        Forest land = new Forest();
        castSteward(List.of(land, aura, shrine));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(aura, shrine);
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.playerDecks.get(player1.getId())).contains(shrine, land);
    }

    @Test
    @DisplayName("Declining the search leaves the library unchanged")
    void decliningSearchLeavesLibraryUnchanged() {
        Cessation aura = new Cessation();
        SanctumOfAll shrine = new SanctumOfAll();
        List<Card> library = List.of(aura, shrine);
        castSteward(library);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura, shrine);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(aura, shrine);
    }

    @Test
    @DisplayName("Does not offer cards that are neither Auras nor Shrines")
    void filtersNonAuraAndNonShrineCards() {
        Forest land = new Forest();
        castSteward(List.of(land));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    private void castSteward(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ShrineSteward()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }
}
