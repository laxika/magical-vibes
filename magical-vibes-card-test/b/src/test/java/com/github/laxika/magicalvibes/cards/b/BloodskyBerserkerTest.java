package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({BloodskyBerserker.class, LightningBolt.class})
class BloodskyBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Your second spell puts two +1/+1 counters on Bloodsky Berserker and grants menace")
    void secondSpellPutsCountersAndGrantsMenace() {
        Permanent berserker = addCreatureReady(player1, new BloodskyBerserker());

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(berserker.getGrantedKeywords()).doesNotContain(Keyword.MENACE);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(berserker.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("Menace granted by Bloodsky Berserker wears off at end of turn")
    void menaceWearsOffAtEndOfTurn() {
        Permanent berserker = addCreatureReady(player1, new BloodskyBerserker());

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(berserker.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    void thirdSpellDoesNotTriggerAgain() {
        Permanent berserker = addCreatureReady(player1, new BloodskyBerserker());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(berserker.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    void opponentsSpellsDoNotTriggerOrCountTowardsYourSecondSpell() {
        Permanent berserker = addCreatureReady(player1, new BloodskyBerserker());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(berserker.getGrantedKeywords()).doesNotContain(Keyword.MENACE);

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(berserker.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    void castingBerserkerAsFirstSpellCountsTowardsSecondSpell() {
        harness.setHand(player1, List.of(new BloodskyBerserker(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent berserker = findPermanent(player1, "Bloodsky Berserker");

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(berserker.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    void castingBerserkerAsSecondSpellDoesNotTriggerItselfOrThirdSpell() {
        harness.setHand(player1, List.of(new LightningBolt(), new BloodskyBerserker(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent berserker = findPermanent(player1, "Bloodsky Berserker");

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(berserker.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    void secondSpellTriggersAgainDuringOpponentsTurn() {
        Permanent berserker = addCreatureReady(player1, new BloodskyBerserker());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(berserker.getGrantedKeywords()).doesNotContain(Keyword.MENACE);

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(berserker.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(berserker.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    void removingSourceInResponseDoesNotPutCountersOnAnotherBerserker() {
        Permanent berserker = addCreatureReady(player1, new BloodskyBerserker());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, berserker.getId());
        harness.assertNotOnBattlefield(player1, "Bloodsky Berserker");
        Permanent otherBerserker = addCreatureReady(player1, new BloodskyBerserker());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(otherBerserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherBerserker.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }
}
