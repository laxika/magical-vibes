package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.t.TreasureMap;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeeprootChampion.class, LightningStrike.class, JungleDelver.class, TreasureMap.class})
class DeeprootChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts a +1/+1 counter on Deeproot Champion")
    void noncreatureSpellAddsCounter() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new DeeprootChampion());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castInstant(player1, 0, player2.getId());

        // Triggered ability should be on the stack
        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Deeproot Champion"))
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve Deeproot Champion trigger
        harness.passBothPriorities(); // resolve Lightning Strike

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Deeproot Champion")
    void creatureSpellDoesNotAddCounter() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new DeeprootChampion());
        harness.setHand(player1, List.of(new JungleDelver()));
        harness.addMana(player1, ManaColor.GREEN, 1);


        harness.castCreature(player1, 0);

        // Only the creature spell should be on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent casting a noncreature spell does not trigger Deeproot Champion")
    void opponentNoncreatureSpellDoesNotAddCounter() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new DeeprootChampion());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);


        harness.castInstant(player2, 0, player1.getId());

        // Only the instant spell should be on the stack, no triggered ability
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple noncreature spells accumulate +1/+1 counters")
    void multipleNoncreatureSpellsAccumulateCounters() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new DeeprootChampion());
        harness.setHand(player1, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 4);


        // Cast first Lightning Strike
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve trigger
        harness.passBothPriorities(); // resolve Lightning Strike

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Cast second Lightning Strike
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve trigger
        harness.passBothPriorities(); // resolve Lightning Strike

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a noncreature permanent spell also adds a counter")
    void artifactSpellAddsCounter() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new DeeprootChampion());
        harness.setHand(player1, List.of(new TreasureMap()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Treasure Map");
    }

    @Test
    @DisplayName("The counter is added before the triggering spell resolves")
    void counterResolvesBeforeSpell() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new DeeprootChampion());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A trigger cannot put a counter on a Champion that has left the battlefield")
    void removedChampionDoesNotGetCounter() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new DeeprootChampion());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, champion.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deeproot Champion");
        harness.assertInGraveyard(player1, "Deeproot Champion");
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Champion gets its own counter from a noncreature spell")
    void multipleChampionsEachGetCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DeeprootChampion());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DeeprootChampion());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(3);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

}
