package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlightSpellbomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaheelisDirective.class, FlightSpellbomb.class, WornPowerstone.class,
        GrizzlyBears.class, Shock.class})
class SaheelisDirectiveTest extends BaseCardTest {

    @Test
    @DisplayName("Puts any number of eligible artifacts onto the battlefield")
    void putsEligibleArtifactsOntoBattlefield() {
        Card spellbomb = new FlightSpellbomb();
        Card powerstone = new WornPowerstone();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        setupLibrary(List.of(spellbomb, powerstone, bears, shock));

        castAndResolve(4);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spellbomb.getId(), powerstone.getId()));

        harness.assertOnBattlefield(player1, "Flight Spellbomb");
        harness.assertOnBattlefield(player1, "Worn Powerstone");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Excludes artifacts with mana value greater than X")
    void excludesArtifactsAboveX() {
        Card powerstone = new WornPowerstone();
        Card spellbomb = new FlightSpellbomb();
        setupLibrary(List.of(powerstone, spellbomb));

        castAndResolve(2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(powerstone.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(spellbomb.getId()));

        harness.assertOnBattlefield(player1, "Flight Spellbomb");
        harness.assertInGraveyard(player1, "Worn Powerstone");
    }

    @Test
    @DisplayName("Putting no artifacts onto the battlefield puts all revealed cards into the graveyard")
    void choosesNoArtifacts() {
        Card spellbomb = new FlightSpellbomb();
        Card bears = new GrizzlyBears();
        setupLibrary(List.of(spellbomb, bears));

        castAndResolve(2);

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Flight Spellbomb");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void setupLibrary(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolve(int xValue) {
        harness.setHand(player1, List.of(new SaheelisDirective()));
        harness.addMana(player1, ManaColor.RED, xValue + 3);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
