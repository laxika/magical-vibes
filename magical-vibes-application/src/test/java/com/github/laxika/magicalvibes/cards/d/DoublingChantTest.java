package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoublingChantTest extends BaseCardTest {

    @Test
    @DisplayName("Each controlled creature offers a search for a same-named creature card put onto the battlefield untapped")
    void fetchesSameNamedCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        setupLibrary();
        castChant();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> "Grizzly Bears".equals(c.getName()));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        // The second Grizzly Bears gets its own search.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Permanent> bears = named("Grizzly Bears");
        assertThat(bears).hasSize(4);
        assertThat(bears).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Each search is optional")
    void mayDeclineSearch() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        setupLibrary();
        castChant();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(named("Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Only creature cards with a matching name are offered — noncreature same-name cards are skipped")
    void skipsNoncreatureNamesAndNonMatchingNames() {
        // A land is not a creature, so it contributes no search; the lone creature's name has no
        // creature copy left in the library, so no search is offered at all.
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new HillGiant());
        setupLibrary();
        castChant();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(named("Forest")).hasSize(1);
    }

    @Test
    @DisplayName("Controlling no creatures runs no search")
    void noCreaturesNoSearch() {
        setupLibrary();
        castChant();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(named("Grizzly Bears")).isEmpty();
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
    }

    private void castChant() {
        harness.setHand(player1, List.of(new DoublingChant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, 0);
    }

    private List<Permanent> named(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .toList();
    }
}
