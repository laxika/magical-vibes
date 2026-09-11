package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KidLoki.class, GrizzlyBears.class, IronshellBeetle.class})
class KidLokiTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creature hexproof after it receives a +1/+1 counter this turn")
    void counteredCreatureGainsHexproof() {
        harness.addToBattlefieldAndReturn(player1, new KidLoki());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();

        castCounterCreature(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Counts a counter put before Kid Loki entered the battlefield")
    void counteredCreatureBeforeKidLokiEntersGainsHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCounterCreature(target);

        harness.addToBattlefieldAndReturn(player1, new KidLoki());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Does not give hexproof to a creature controlled by an opponent")
    void opponentCreatureDoesNotGainHexproof() {
        harness.addToBattlefieldAndReturn(player1, new KidLoki());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCounterCreature(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when you draw your second card")
    void triggersOnSecondCardDrawn() {
        Permanent kidLoki = harness.addToBattlefieldAndReturn(player1, new KidLoki());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawAndResolveTrigger(player1);
        assertThat(kidLoki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        drawAndResolveTrigger(player1);
        assertThat(kidLoki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        drawAndResolveTrigger(player1);
        assertThat(kidLoki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castCounterCreature(Permanent target) {
        harness.setHand(player1, List.of(new IronshellBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
