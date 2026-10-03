package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CollectiveUnconscious.class, GrizzlyBears.class, Forest.class, Unsummon.class})
class CollectiveUnconsciousTest extends BaseCardTest {

    @Test
    @DisplayName("Counts creatures at resolution after a creature is returned in response")
    void countsCreaturesAtResolution() {
        var returnedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new CollectiveUnconscious(), "{4}{G}{G}");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, returnedCreature.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertInGraveyard(player1, "Collective Unconscious");
    }

    @Test
    @DisplayName("Draws a card for each creature the controller controls")
    void drawsForEachCreatureControlled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new CollectiveUnconscious(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Does not count creatures opponents control")
    void onlyCountsControllerCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new CollectiveUnconscious(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws no cards when controller controls no creatures")
    void drawsNoCardsWithoutCreatures() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new CollectiveUnconscious(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Does not count noncreature permanents")
    void doesNotCountNoncreaturePermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new CollectiveUnconscious(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Counts creatures but not other permanents")
    void ignoresNonCreaturePermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new CollectiveUnconscious(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }
}
