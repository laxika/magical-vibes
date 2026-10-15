package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnigmaSphinx.class, Plains.class, Mountain.class, Forest.class, WrathOfGod.class,
        EsperStormblade.class})
class EnigmaSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("When Enigma Sphinx dies it is tucked into its owner's library third from the top")
    void diesGoesThirdFromTop() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new EnigmaSphinx());
        Card sphinxCard = sphinx.getCard();

        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));

        killWithWrath();

        // Inserted at index 2 (third from the top): [top, second, sphinx, third].
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.get(2).getId()).isEqualTo(sphinxCard.getId());
        assertThat(library).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), sphinxCard.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(sphinxCard.getId()));
    }

    @Test
    @DisplayName("With fewer cards than the target position, Enigma Sphinx is placed on the bottom of the library")
    void tooFewCardsGoesToBottom() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new EnigmaSphinx());
        Card sphinxCard = sphinx.getCard();

        Card only = new Plains();
        harness.setLibrary(player1, List.of(only));

        killWithWrath();

        // Position 2 clamps to the bottom of a 1-card library: [only, sphinx].
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).extracting(Card::getId).containsExactly(only.getId(), sphinxCard.getId());
    }

    @Test
    void deathIntoEmptyLibraryPutsSphinxOnBottom() {
        Card sphinx = new EnigmaSphinx();
        harness.addToBattlefield(player1, sphinx);
        harness.setLibrary(player1, List.of());
        killWithWrath();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sphinx);
    }

    @Test
    void opponentControlledSphinxDoesNotTriggerWhenItDies() {
        Card sphinx = new EnigmaSphinx();
        sphinx.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, sphinx);
        harness.setLibrary(player1, List.of(new Plains(), new Mountain(), new Forest()));
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sphinx);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(sphinx);
    }

    @Test
    void deathTriggerDoesNothingIfSphinxLeavesGraveyard() {
        Card sphinx = new EnigmaSphinx();
        harness.addToBattlefield(player1, sphinx);
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top));
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        gd.playerGraveyards.get(player1.getId()).remove(sphinx);
        harness.setExile(player1, List.of(sphinx));
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.exiledCards).extracting(e -> e.card().getId()).contains(sphinx.getId());
    }

    @Test
    void cascadeExilesSkippedCardsAndHit() {
        Card land = new Plains();
        Card hit = new EsperStormblade();
        harness.setLibrary(player1, List.of(land, hit));
        harness.castFromHand(player1, new EnigmaSphinx(), "{4}{W}{U}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        assertThat(gd.exiledCards).extracting(e -> e.card().getId())
                .containsExactlyInAnyOrder(land.getId(), hit.getId());
        assertThat(gd.cardsExiledThisTurn).isEqualTo(2);
    }

    @Test
    void cascadeCastsHitForFreeBeforeSphinxResolves() {
        Card land = new Plains();
        Card hit = new EsperStormblade();
        Card below = new Forest();
        harness.setLibrary(player1, List.of(land, hit, below));
        harness.castFromHand(player1, new EnigmaSphinx(), "{4}{W}{U}{B}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(hit.getId());
        assertThat(gd.playersWhoPlayedCardFromExileThisTurn).contains(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, land);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Esper Stormblade");
        harness.assertNotOnBattlefield(player1, "Enigma Sphinx");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Enigma Sphinx");
    }

    @Test
    void cascadeDeclineReturnsSkippedCardsAndHitBelowUntouchedCards() {
        Card land = new Plains();
        Card equal = new EnigmaSphinx();
        Card hit = new EsperStormblade();
        Card below = new Forest();
        harness.setLibrary(player1, List.of(land, equal, hit, below));
        harness.castFromHand(player1, new EnigmaSphinx(), "{4}{W}{U}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(below);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(below, land, equal, hit);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Enigma Sphinx");
        harness.assertNotOnBattlefield(player1, "Esper Stormblade");
    }

    @Test
    void cascadeWithNoHitReturnsAllCards() {
        Card land = new Plains();
        Card equal = new EnigmaSphinx();
        harness.setLibrary(player1, List.of(land, equal));
        harness.castFromHand(player1, new EnigmaSphinx(), "{4}{W}{U}{B}");
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, equal);
        assertThat(gd.cardsExiledThisTurn).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Enigma Sphinx");
    }

    @Test
    void cascadeWithEmptyLibraryStillResolvesSphinx() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new EnigmaSphinx(), "{4}{W}{U}{B}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Enigma Sphinx");
    }

    private void killWithWrath() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Wrath resolves — the Sphinx dies, death trigger goes on the stack
        harness.passBothPriorities(); // resolve the death trigger — tuck into library
    }
}
