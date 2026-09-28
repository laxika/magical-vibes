package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcaneArchery.class, FountainOfYouth.class, GrizzlyBears.class})
class ArcaneArcheryTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +3/+3 and reach and trample until end of turn")
    void boostsAndGrantsKeywordsUntilEndOfTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castArcaneArchery(bear);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The one-time boon gives the next creature spell all three counters")
    void empowersNextCreatureSpellOnce() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of(new ArcaneArchery(), first, second));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent empowered = findPermanent(first);
        assertThat(empowered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(empowered.getCounterCount(CounterType.REACH)).isEqualTo(1);
        assertThat(empowered.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent unempowered = findPermanent(second);
        assertThat(unempowered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unempowered.getCounterCount(CounterType.REACH)).isZero();
        assertThat(unempowered.getCounterCount(CounterType.TRAMPLE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ArcaneArchery()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castArcaneArchery(Permanent target) {
        harness.setHand(player1, List.of(new ArcaneArchery()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent findPermanent(GrizzlyBears card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
