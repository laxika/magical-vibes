package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BulwarkOxTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled puts a +1/+1 counter on target creature")
    void attacksWhileSaddledPutsCounterOnTargetCreature() {
        Permanent ox = addCreatureReady(player1, new BulwarkOx());
        ox.setSaddled(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking while not saddled does not put a counter on a creature")
    void attacksWhileNotSaddledDoesNotPutCounter() {
        addCreatureReady(player1, new BulwarkOx());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing the Ox protects your creatures with counters until end of turn")
    void sacrificeGrantsKeywordsToCounteredCreaturesUntilEndOfTurn() {
        addCreatureReady(player1, new BulwarkOx());
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bulwark Ox");
        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
