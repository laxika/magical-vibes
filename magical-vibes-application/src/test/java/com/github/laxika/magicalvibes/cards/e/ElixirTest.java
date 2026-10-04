package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TownGreeter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Elixir.class, Forest.class, TownGreeter.class})
class ElixirTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new Elixir()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiles itself, shuffles nonland graveyard cards, and gains that much life")
    void exilesSelfShufflesNonlandsAndGainsLife() {
        harness.addToBattlefield(player1, new Elixir());
        Card creature = new TownGreeter();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land, new TownGreeter()));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elixir");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Elixir"));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Does not gain life for a graveyard containing only lands")
    void doesNotGainLifeForLands() {
        harness.addToBattlefield(player1, new Elixir());
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot activate without paying five mana")
    void requiresFiveMana() {
        harness.addToBattlefield(player1, new Elixir());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent elixir = harness.addToBattlefieldAndReturn(player1, new Elixir());
        elixir.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Elixir");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void emptyGraveyardGainsNoLife() {
        harness.addToBattlefield(player1, new Elixir());
        harness.setGraveyard(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Elixir"));
    }

    @Test
    void exilesAsCostAndUsesControllersGraveyardAtResolution() {
        harness.addToBattlefield(player1, new Elixir());
        Card original = new TownGreeter();
        Card addedBeforeResolution = new Elixir();
        Card opposingCard = new TownGreeter();
        harness.setGraveyard(player1, List.of(original));
        harness.setGraveyard(player2, List.of(opposingCard));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        int opposingLibrarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opposingLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Elixir");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Elixir"));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(original);
        harness.assertLife(player1, lifeBefore);
        gd.playerGraveyards.get(player1.getId()).add(addedBeforeResolution);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(librarySizeBefore + 2).contains(original, addedBeforeResolution);
        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opposingLibrarySizeBefore);
        harness.assertLife(player2, opposingLifeBefore);
    }
}
