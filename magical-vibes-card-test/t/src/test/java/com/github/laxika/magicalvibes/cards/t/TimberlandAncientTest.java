package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SkyclaveAerialist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimberlandAncient.class, Forest.class, SkyclaveAerialist.class})
class TimberlandAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Forestcycling discards the card and offers only Forest cards")
    void forestcyclingDiscardsAndOffersForests() {
        harness.setHand(player1, List.of(new TimberlandAncient()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new TimberlandAncient()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Timberland Ancient");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Choosing a Forest from Forestcycling puts it into hand")
    void choosingForestPutsItIntoHand() {
        harness.setHand(player1, List.of(new TimberlandAncient()));
        harness.setLibrary(player1, List.of(new Forest(), new TimberlandAncient()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Timberland Ancient");
    }

    @Test
    void discardIsPaidBeforeForestcyclingResolves() {
        harness.setHand(player1, List.of(new TimberlandAncient()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Timberland Ancient");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void insufficientManaDoesNotDiscardSource() {
        harness.setHand(player1, List.of(new TimberlandAncient()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Timberland Ancient");
        harness.assertNotInGraveyard(player1, "Timberland Ancient");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayFailToFindEvenWhenForestIsAvailable() {
        harness.setHand(player1, List.of(new TimberlandAncient()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Timberland Ancient");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void forestcyclingResolvesWithoutAnyForestInLibrary() {
        harness.setHand(player1, List.of(new TimberlandAncient()));
        harness.setLibrary(player1, List.of(new TimberlandAncient()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Timberland Ancient");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new SkyclaveAerialist());
        addCreatureReady(player2, new TimberlandAncient());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Skyclave Aerialist");
        harness.assertOnBattlefield(player2, "Timberland Ancient");
    }

    @Test
    void trampleDealsExcessDamageAfterLethalDamageToBlocker() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new TimberlandAncient());
        Permanent blocker = addCreatureReady(player2, new SkyclaveAerialist());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 5));

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Skyclave Aerialist");
        harness.assertOnBattlefield(player1, "Timberland Ancient");
    }
}
