package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JoinTheRanks;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SouthPoleVoyager.class, JoinTheRanks.class, GrizzlyBears.class, Conspiracy.class})
class SouthPoleVoyagerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life for each Ally that enters and draws on the second trigger resolution")
    void gainsLifeAndDrawsOnSecondAllyEntryTriggerResolution() {
        Permanent voyager = addCreatureReady(player1, new SouthPoleVoyager());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new JoinTheRanks(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(voyager).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("The Voyager's own entry triggers once without drawing")
    void ownEntryOnlyGainsLife() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new SouthPoleVoyager(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a non-Ally creature")
    void doesNotTriggerForNonAlly() {
        addCreatureReady(player1, new SouthPoleVoyager());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsOnlyOnSecondResolutionNotThirdOrFourth() {
        addCreatureReady(player1, new SouthPoleVoyager());
        SouthPoleVoyager firstCard = new SouthPoleVoyager();
        SouthPoleVoyager secondCard = new SouthPoleVoyager();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new JoinTheRanks(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);

        harness.castFromHand(player1, new JoinTheRanks(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    void eachVoyagerCountsItsOwnResolutions() {
        addCreatureReady(player1, new SouthPoleVoyager());
        addCreatureReady(player1, new SouthPoleVoyager());
        SouthPoleVoyager firstCard = new SouthPoleVoyager();
        SouthPoleVoyager secondCard = new SouthPoleVoyager();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new JoinTheRanks(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstCard, secondCard);
    }

    @Test
    void doesNotTriggerForOpponentsAllies() {
        addCreatureReady(player1, new SouthPoleVoyager());
        harness.setLibrary(player1, List.of(new SouthPoleVoyager()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new JoinTheRanks(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void ownEntryTriggersEvenWhenConspiracyReplacesItsAllyType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        harness.setLibrary(player1, List.of(new SouthPoleVoyager()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new SouthPoleVoyager(), "{1}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
