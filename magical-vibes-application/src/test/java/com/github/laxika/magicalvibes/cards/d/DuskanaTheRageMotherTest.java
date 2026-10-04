package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskanaTheRageMother.class, GrizzlyBears.class, HillGiant.class})
class DuskanaTheRageMotherTest extends BaseCardTest {

    @Test
    @DisplayName("Duskana draws for each own creature with base power and toughness 2/2")
    void drawsForOwnTwoTwoCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        castDuskana();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Duskana boosts each attacking own 2/2 creature")
    void boostsAttackingTwoTwoCreatures() {
        addCreatureReady(player1, new DuskanaTheRageMother());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Duskana's attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new DuskanaTheRageMother());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Duskana draws nothing when only opposing creatures qualify")
    void drawsNothingWithoutQualifyingControlledCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears undrawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(undrawn));

        castDuskana();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    @DisplayName("Counters and additive modifiers do not change the base stats used for drawing")
    void drawsForModifiedBaseTwoTwoCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bear.setPowerModifier(3);
        bear.setToughnessModifier(3);
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.setPowerModifier(-1);
        giant.setToughnessModifier(-1);
        GrizzlyBears drawn = new GrizzlyBears();
        HillGiant undrawn = new HillGiant();
        harness.setLibrary(player1, List.of(drawn, undrawn));

        castDuskana();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    @DisplayName("The entry trigger counts qualifying creatures when it resolves")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears firstDraw = new GrizzlyBears();
        HillGiant secondDraw = new HillGiant();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        castDuskana();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Each qualifying attacker is boosted even when it has counters")
    void boostsEachQualifyingAttackerWithCounters() {
        addCreatureReady(player1, new DuskanaTheRageMother());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        firstBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttackingBear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, firstBear)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, secondBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondBear)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonAttackingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonAttackingBear)).isEqualTo(2);
    }

    private void castDuskana() {
        harness.castFromHand(player1, new DuskanaTheRageMother(), "{2}{R}{G}{W}");
    }
}
