package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMasterOfLakeTown.class, GrizzlyBears.class, Shock.class})
class TheMasterOfLakeTownTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever an opponent loses life, that player mills that many cards")
    void opponentLifeLossCausesMill() {
        harness.addToBattlefield(player1, new TheMasterOfLakeTown());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Whenever its controller loses life, that player mills that many cards")
    void controllerLifeLossCausesMill() {
        harness.addToBattlefield(player1, new TheMasterOfLakeTown());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("When it dies, its controller draws for each graveyard with at least seven cards")
    void deathDrawsForQualifyingGraveyards() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new TheMasterOfLakeTown());
        harness.setGraveyard(player1, graveyardOf(6));
        harness.setGraveyard(player2, graveyardOf(7));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        master.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Life-loss milling waits for its triggered ability to resolve")
    void millingUsesTheStack() {
        harness.addToBattlefield(player1, new TheMasterOfLakeTown());
        harness.setLibrary(player2, graveyardOf(4));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Controller life-loss milling also waits for resolution")
    void controllerMillingUsesTheStack() {
        harness.addToBattlefield(player1, new TheMasterOfLakeTown());
        harness.setLibrary(player1, graveyardOf(4));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Milling more cards than remain empties the library")
    void millsOnlyAvailableCards() {
        harness.addToBattlefield(player1, new TheMasterOfLakeTown());
        harness.setLibrary(player2, graveyardOf(1));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The death ability draws nothing when neither graveyard qualifies")
    void deathDrawsNothingBelowThreshold() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new TheMasterOfLakeTown());
        harness.setGraveyard(player1, graveyardOf(5));
        harness.setGraveyard(player2, graveyardOf(6));
        harness.setLibrary(player1, graveyardOf(3));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        master.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The death ability counts qualifying graveyards at resolution")
    void deathChecksGraveyardsAtResolution() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new TheMasterOfLakeTown());
        harness.setGraveyard(player1, graveyardOf(5));
        harness.setGraveyard(player2, graveyardOf(6));
        harness.setLibrary(player1, graveyardOf(3));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        master.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.setGraveyard(player2, graveyardOf(7));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    private List<Card> graveyardOf(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
