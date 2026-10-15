package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SelesnyaSanctuary;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerilousForays.class, Forest.class, Watchwolf.class, TempleGarden.class, SelesnyaSanctuary.class})
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

        harness.handleCardChosen(player1, 0);

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

    @Test
    @DisplayName("Pays the sacrifice cost before the ability resolves and puts a basic land onto the battlefield tapped")
    void sacrificesBeforeResolutionAndFindsBasicLand() {
        harness.addToBattlefield(player1, new Watchwolf());
        harness.addToBattlefield(player1, new PerilousForays());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 1, 0, null, null);

        harness.assertInGraveyard(player1, "Watchwolf");
        harness.assertNotOnBattlefield(player1, "Watchwolf");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can fail to find even when a matching land is present")
    void canDeclineToFindMatchingLand() {
        harness.addToBattlefield(player1, new Watchwolf());
        harness.addToBattlefield(player1, new PerilousForays());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Watchwolf");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A land without a basic land type cannot be found")
    void excludesLandWithoutBasicLandType() {
        harness.addToBattlefield(player1, new Watchwolf());
        harness.addToBattlefield(player1, new PerilousForays());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new SelesnyaSanctuary(), new Forest()));

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Selesnya Sanctuary");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Selesnya Sanctuary");
    }

    @Test
    @DisplayName("Temple Garden remains tapped even if its controller pays two life")
    void templeGardenRemainsTappedWhenLifeIsPaid() {
        harness.addToBattlefield(player1, new Watchwolf());
        harness.addToBattlefield(player1, new PerilousForays());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new TempleGarden()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        assertThat(findPermanent(player1, "Temple Garden").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot use an opponent's creature to pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new PerilousForays());
        harness.addToBattlefield(player2, new Watchwolf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Watchwolf");
    }

    @Test
    @DisplayName("Cannot activate without paying one mana")
    void requiresManaPayment() {
        harness.addToBattlefield(player1, new Watchwolf());
        harness.addToBattlefield(player1, new PerilousForays());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Watchwolf");
        assertThat(gd.stack).isEmpty();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new TempleGarden(), new Forest(), new Watchwolf()));
    }
}
