package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiringCall.class, BearCub.class})
class InspiringCallTest extends BaseCardTest {

    @Test
    @DisplayName("Draws for and grants indestructible to creatures you control with +1/+1 counters")
    void drawsAndProtectsCounteredCreatures() {
        Permanent counteredCreature = addCreature(player1, true);
        Permanent uncounteredCreature = addCreature(player1, false);
        Permanent differentlyCounteredCreature = addCreature(player1, false);
        differentlyCounteredCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        addCreature(player2, true);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new InspiringCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, differentlyCounteredCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The protection ends at the end of the turn")
    void indestructibleEndsAtEndOfTurn() {
        Permanent counteredCreature = addCreature(player1, true);

        harness.setHand(player1, List.of(new InspiringCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Counts creatures, not the number of +1/+1 counters")
    void drawsOncePerQualifyingCreature() {
        Permanent first = addCreature(player1, true);
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent second = addCreature(player1, true);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new InspiringCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Resolves without drawing or protecting when only opposing creatures qualify")
    void noQualifyingOwnCreatures() {
        Permanent own = addCreature(player1, false);
        Permanent opposing = addCreature(player2, true);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new InspiringCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gqs.hasKeyword(gd, own, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Determines qualifying creatures when the spell resolves")
    void checksCountersAtResolution() {
        Permanent losesCounter = addCreature(player1, true);
        Permanent gainsCounter = addCreature(player1, false);
        harness.setHand(player1, List.of(new InspiringCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);

        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        gainsCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.hasKeyword(gd, losesCounter, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, gainsCounter, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Protection stays with the creatures that qualified at resolution")
    void protectionDoesNotFollowLaterCounterChanges() {
        Permanent protectedCreature = addCreature(player1, true);
        Permanent unprotectedCreature = addCreature(player1, false);
        harness.setHand(player1, List.of(new InspiringCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        protectedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        unprotectedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent laterCreature = addCreature(player1, true);

        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unprotectedCreature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addCreature(Player player, boolean withCounter) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new BearCub());
        if (withCounter) {
            creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        }
        return creature;
    }
}
