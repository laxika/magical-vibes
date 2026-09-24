package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.d.DarksteelPendant;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reshape.class, CrazedGoblin.class, DarksteelCitadel.class, DarksteelIngot.class,
        DarksteelPendant.class, MyrMoonvessel.class})
class ReshapeTest extends BaseCardTest {

    @Test
    @DisplayName("Reshape sacrifices an artifact and searches for an artifact with mana value X or less")
    void sacrificesArtifactAndFindsMatchingArtifact() {
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        DarksteelPendant foundArtifact = new DarksteelPendant();
        MyrMoonvessel tooSmallArtifact = new MyrMoonvessel();
        DarksteelIngot tooExpensiveArtifact = new DarksteelIngot();
        CrazedGoblin nonArtifact = new CrazedGoblin();
        harness.setLibrary(player1, List.of(foundArtifact, tooSmallArtifact, tooExpensiveArtifact, nonArtifact));
        harness.setHand(player1, List.of(new Reshape()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(), false, sacrificedArtifact.getId());

        harness.assertNotOnBattlefield(player1, "Darksteel Ingot");
        harness.assertInGraveyard(player1, "Darksteel Ingot");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(foundArtifact, tooSmallArtifact);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        int foundIndex = search.params().cards().indexOf(foundArtifact);
        harness.handleCardChosen(player1, foundIndex);

        harness.assertOnBattlefield(player1, "Darksteel Pendant");
        assertThat(findPermanent(player1, "Darksteel Pendant").isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Darksteel Ingot");
        harness.assertNotOnBattlefield(player1, "Crazed Goblin");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(tooSmallArtifact, tooExpensiveArtifact, nonArtifact);
    }

    @Test
    @DisplayName("Reshape can search for a zero-mana artifact when X is zero")
    void findsZeroManaValueArtifactAtZero() {
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        DarksteelCitadel zeroManaArtifact = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(zeroManaArtifact, new DarksteelPendant(), new CrazedGoblin()));
        harness.setHand(player1, List.of(new Reshape()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrificedArtifact.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(zeroManaArtifact);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Darksteel Citadel");
        assertThat(findPermanent(player1, "Darksteel Citadel").isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Reshape finishes and shuffles when its library search has no matching artifact")
    void finishesWhenNoMatchingArtifactExists() {
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        CrazedGoblin nonArtifact = new CrazedGoblin();
        harness.setLibrary(player1, List.of(nonArtifact));
        harness.setHand(player1, List.of(new Reshape()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrificedArtifact.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Crazed Goblin");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonArtifact);
    }

    @Test
    @DisplayName("Reshape cannot be cast without an artifact to sacrifice")
    void requiresArtifactSacrifice() {
        harness.addToBattlefield(player1, new CrazedGoblin());
        harness.setHand(player1, List.of(new Reshape()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Reshape");
        harness.assertOnBattlefield(player1, "Crazed Goblin");
    }
}
