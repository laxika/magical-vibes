package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrimordialGnawer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuratorOfSunsCreation.class, PrimordialGnawer.class, Shock.class, Forest.class,
        GrizzlyBears.class, CentaurCourser.class})
class CuratorOfSunsCreationTest extends BaseCardTest {

    @Test
    @DisplayName("Discovers again for the same value and only triggers once each turn")
    void discoversAgainForSameValueOnceEachTurn() {
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new CuratorOfSunsCreation());
        Permanent gnawer = harness.addToBattlefieldAndReturn(player1, new PrimordialGnawer());
        GrizzlyBears firstDiscovered = new GrizzlyBears();
        CentaurCourser secondDiscovered = new CentaurCourser();
        harness.setLibrary(player1, List.of(new Forest(), firstDiscovered, new Forest(), secondDiscovered));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, gnawer.getId());
        harness.passBothPriorities();

        chooseDiscoveredCard(-1);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == curator.getCard());

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch secondDiscover =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondDiscover).isNotNull();
        assertThat(secondDiscover.params().cards()).containsExactly(secondDiscovered);

        chooseDiscoveredCard(-1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == curator.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDiscovered, secondDiscovered);
    }

    private void chooseDiscoveredCard(int cardIndex) {
        harness.handleCardChosen(player1, cardIndex);
    }

    @Test
    @DisplayName("Casting the discovered card puts the extra discover above that spell")
    void triggersWhenDiscoveredCardIsCast() {
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new CuratorOfSunsCreation());
        Permanent gnawer = harness.addToBattlefieldAndReturn(player1, new PrimordialGnawer());
        GrizzlyBears firstDiscovered = new GrizzlyBears();
        CentaurCourser secondDiscovered = new CentaurCourser();
        harness.setLibrary(player1, List.of(firstDiscovered, secondDiscovered));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, gnawer.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard()).isSameAs(curator.getCard());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch extraDiscover =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(extraDiscover).isNotNull();
        assertThat(extraDiscover.params().cards()).containsExactly(secondDiscovered);
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(secondDiscovered);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discovering with no qualifying card still triggers once")
    void triggersWithoutAQualifyingCard() {
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new CuratorOfSunsCreation());
        Permanent gnawer = harness.addToBattlefieldAndReturn(player1, new PrimordialGnawer());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, gnawer.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getCard()).isSameAs(curator.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("An opponent discovering does not trigger Curator")
    void doesNotTriggerForOpponentDiscover() {
        harness.addToBattlefield(player1, new CuratorOfSunsCreation());
        Permanent gnawer = harness.addToBattlefieldAndReturn(player2, new PrimordialGnawer());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player2, List.of(discovered));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, gnawer.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player2.getId())).contains(discovered);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The once-per-turn limit resets on the opponent's turn")
    void triggersAgainOnTheNextTurn() {
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new CuratorOfSunsCreation());
        Permanent firstGnawer = harness.addToBattlefieldAndReturn(player1, new PrimordialGnawer());
        Permanent secondGnawer = harness.addToBattlefieldAndReturn(player1, new PrimordialGnawer());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, firstGnawer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.passUntil(player2, TurnStep.UPKEEP);
        GrizzlyBears firstDiscovered = new GrizzlyBears();
        CentaurCourser secondDiscovered = new CentaurCourser();
        harness.setLibrary(player1, List.of(firstDiscovered, secondDiscovered));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, secondGnawer.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == curator.getCard());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDiscovered, secondDiscovered);
        assertThat(gd.stack).isEmpty();
    }
}
