package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.g.GarruksCompanion;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoublingChant.class, RuneclawBear.class, GarruksCompanion.class, Forest.class, PsychogenicProbe.class})
class DoublingChantTest extends BaseCardTest {

    @Test
    @DisplayName("Each controlled creature offers a search for a same-named creature card put onto the battlefield untapped")
    void fetchesSameNamedCreatures() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new RuneclawBear());
        setupLibrary();
        castAndResolveChant();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> "Runeclaw Bear".equals(c.getName()));

        harness.handleCardChosen(player1, 0);
        // The second Runeclaw Bear gets its own search.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Permanent> bears = named("Runeclaw Bear");
        assertThat(bears).hasSize(4);
        assertThat(bears).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Each search is optional")
    void mayDeclineSearch() {
        harness.addToBattlefield(player1, new RuneclawBear());
        setupLibrary();
        castAndResolveChant();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(named("Runeclaw Bear")).hasSize(1);
    }

    @Test
    @DisplayName("Only creature cards with a matching name are offered — noncreature same-name cards are skipped")
    void skipsNoncreatureNamesAndNonMatchingNames() {
        // A land is not a creature, so it contributes no search; the lone creature's name has no
        // creature copy left in the library, so no search is offered at all.
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GarruksCompanion());
        setupLibrary();
        castAndResolveChant();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(named("Forest")).hasSize(1);
    }

    @Test
    @DisplayName("Controlling no creatures runs no search")
    void noCreaturesNoSearch() {
        setupLibrary();
        castAndResolveChant();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(named("Runeclaw Bear")).isEmpty();
    }

    @Test
    @DisplayName("Chosen creatures stay off the battlefield until all searches are complete")
    void putsChosenCreaturesOntoBattlefieldTogether() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new GarruksCompanion());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new GarruksCompanion()));
        castAndResolveChant();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(named("Runeclaw Bear")).hasSize(1);
        assertThat(named("Garruk's Companion")).hasSize(1);

        harness.handleCardChosen(player1, 0);

        assertThat(named("Runeclaw Bear")).hasSize(2);
        assertThat(named("Garruk's Companion")).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The library is shuffled once after all creatures are found")
    void shufflesOnlyOnceForMultipleCreatures() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setLife(player1, 20);
        setupLibrary();
        castAndResolveChant();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The library is shuffled even when no creatures are controlled")
    void shufflesWithNoCreatures() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        setupLibrary();
        castAndResolveChant();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Opponent creatures do not grant searches")
    void ignoresOpponentCreatures() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new GarruksCompanion());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new GarruksCompanion()));
        castAndResolveChant();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(named("Runeclaw Bear")).hasSize(2);
        assertThat(named("Garruk's Companion")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(c -> c.getName())
                .containsExactly("Garruk's Companion");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear(), new Forest()));
    }

    private void castAndResolveChant() {
        harness.setHand(player1, List.of(new DoublingChant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private List<Permanent> named(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .toList();
    }
}
