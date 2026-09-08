package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addCreature(Player player, boolean withCounter) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        if (withCounter) {
            creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        }
        return creature;
    }
}
