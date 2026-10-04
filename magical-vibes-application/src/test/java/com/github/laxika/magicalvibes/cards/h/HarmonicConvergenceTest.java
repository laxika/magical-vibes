package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.i.ImpendingDisaster;
import com.github.laxika.magicalvibes.cards.r.Rivalry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmonicConvergence.class, GiantCockroach.class, ImpendingDisaster.class, Rivalry.class})
class HarmonicConvergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts all enchantments on top of their owners' libraries")
    void putsAllEnchantmentsOnTopOfOwnersLibraries() {
        harness.addToBattlefield(player1, new Rivalry());
        harness.addToBattlefield(player2, new ImpendingDisaster());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.setLibrary(player2, List.of(new GiantCockroach()));

        cast();

        harness.assertNotOnBattlefield(player1, "Rivalry");
        harness.assertNotOnBattlefield(player2, "Impending Disaster");
        harness.assertOnBattlefield(player1, "Giant Cockroach");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Rivalry");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Impending Disaster");
        harness.assertInGraveyard(player1, "Harmonic Convergence");
    }

    @Test
    @DisplayName("Each owner chooses the order of multiple enchantments")
    void ownerChoosesOrderOfMultipleEnchantments() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Rivalry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        harness.setLibrary(player1, List.of(new GiantCockroach()));

        cast();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId(), first.getId()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Impending Disaster");
        assertThat(gd.playerDecks.get(player1.getId()).get(1).getName()).isEqualTo("Rivalry");
        harness.assertInGraveyard(player1, "Harmonic Convergence");
    }

    @Test
    @DisplayName("Each owner orders their own enchantments independently")
    void eachOwnerOrdersTheirOwnEnchantmentsIndependently() {
        Permanent player1First = harness.addToBattlefieldAndReturn(player1, new Rivalry());
        Permanent player1Second = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        Permanent player2First = harness.addToBattlefieldAndReturn(player2, new Rivalry());
        Permanent player2Second = harness.addToBattlefieldAndReturn(player2, new ImpendingDisaster());
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.setLibrary(player2, List.of(new GiantCockroach()));

        cast();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.validIds()).containsExactly(player1First.getId(), player1Second.getId());
        harness.handleMultiplePermanentsChosen(player1,
                List.of(player1Second.getId(), player1First.getId()));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.validIds()).containsExactly(player2First.getId(), player2Second.getId());
        harness.handleMultiplePermanentsChosen(player2,
                List.of(player2Second.getId(), player2First.getId()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Impending Disaster");
        assertThat(gd.playerDecks.get(player1.getId()).get(1).getName()).isEqualTo("Rivalry");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Impending Disaster");
        assertThat(gd.playerDecks.get(player2.getId()).get(1).getName()).isEqualTo("Rivalry");
        harness.assertInGraveyard(player1, "Harmonic Convergence");
    }

    @Test
    @DisplayName("Resolves without enchantments and leaves libraries unchanged")
    void resolvesWithoutEnchantments() {
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.setLibrary(player2, List.of(new GiantCockroach()));

        cast();

        harness.assertOnBattlefield(player1, "Giant Cockroach");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Giant Cockroach");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Giant Cockroach");
        harness.assertInGraveyard(player1, "Harmonic Convergence");
    }

    @Test
    @DisplayName("Returns an enchantment to its owner rather than its controller")
    void returnsEnchantmentToOwner() {
        Rivalry stolen = new Rivalry();
        stolen.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, stolen);
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.setLibrary(player2, List.of(new GiantCockroach()));

        cast();

        harness.assertNotOnBattlefield(player2, "Rivalry");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Rivalry", "Giant Cockroach");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Giant Cockroach");
    }

    @Test
    @DisplayName("The active player chooses their library order first on the opponent's turn")
    void activePlayerChoosesFirstOnOpponentsTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Rivalry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        Permanent opponentFirst = harness.addToBattlefieldAndReturn(player2, new Rivalry());
        Permanent opponentSecond = harness.addToBattlefieldAndReturn(player2, new ImpendingDisaster());
        gd.activePlayerId = player2.getId();

        cast();

        PendingInteraction.MultiPermanentChoice activeChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(activeChoice).isNotNull();
        assertThat(activeChoice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2,
                List.of(opponentSecond.getId(), opponentFirst.getId()));
        PendingInteraction.MultiPermanentChoice nonactiveChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(nonactiveChoice).isNotNull();
        assertThat(nonactiveChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId(), first.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Impending Disaster");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Impending Disaster");
        harness.assertInGraveyard(player1, "Harmonic Convergence");
    }

    private void cast() {
        harness.castFromHand(player1, new HarmonicConvergence(), "{2}{G}");
        harness.passBothPriorities();
    }
}
