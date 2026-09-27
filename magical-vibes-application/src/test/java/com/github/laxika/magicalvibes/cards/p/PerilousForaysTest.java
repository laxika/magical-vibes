package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerilousForays.class, Forest.class, Watchwolf.class, TempleGarden.class})
class PerilousForaysTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and searches for a land with a basic land type")
    void searchesForLandWithBasicLandType() {
        harness.addToBattlefield(player1, new Watchwolf());
        harness.addToBattlefield(player1, new PerilousForays());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Watchwolf");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Temple Garden", "Forest")
                .doesNotContain("Watchwolf");
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Temple Garden").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new PerilousForays());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifices the creature even when no land matches")
    void sacrificesCreatureWhenNoLandMatches() {
        harness.addToBattlefield(player1, new Watchwolf());
        harness.addToBattlefield(player1, new PerilousForays());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Watchwolf()));

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Watchwolf");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Watchwolf");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Watchwolf");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new TempleGarden(), new Forest(), new Watchwolf()));
    }
}
