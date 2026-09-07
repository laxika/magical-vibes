package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToxrillTheCorrosive.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class ToxrillTheCorrosiveTest extends BaseCardTest {

    @Test
    @DisplayName("The end-step trigger adds slime counters only to opponents' creatures")
    void endStepAddsSlimeCountersToOpponentsCreatures() {
        harness.addToBattlefield(player1, new ToxrillTheCorrosive());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new AirElemental());
        opponentCreature.setCounterCount(CounterType.SLIME, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.SLIME)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.SLIME)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A slimed opponent creature's death creates exactly one Slug token")
    void slimedOpponentCreatureDeathCreatesOneSlug() {
        harness.addToBattlefield(player1, new ToxrillTheCorrosive());
        Permanent opponentCreature = addCreatureReady(player2, new AirElemental());
        opponentCreature.setCounterCount(CounterType.SLIME, 2);
        opponentCreature.setMarkedDamage(2);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Slug")).hasSize(1);
    }

    @Test
    @DisplayName("A creature without a slime counter does not create a Slug token when it dies")
    void creatureWithoutSlimeCounterDoesNotCreateSlug() {
        harness.addToBattlefield(player1, new ToxrillTheCorrosive());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Slug")).isEmpty();
    }

    @Test
    @DisplayName("The activated ability can sacrifice Toxrill itself and draw a card")
    void sacrificeSlugDrawsCard() {
        Permanent source = addCreatureReady(player1, new ToxrillTheCorrosive());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }
}
