package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StirringHopesinger.class, HillGiant.class, Shock.class, SpittingEarth.class})
class StirringHopesingerTest extends BaseCardTest {
    @Test
    @DisplayName("Casting an instant that targets a creature adds a counter to each creature you control")
    void reparteeBuffsAllCreatures() {
        Permanent hopesinger = harness.addToBattlefieldAndReturn(player1, new StirringHopesinger());
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent oppGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, ownGiant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hopesinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Opponent's creature is unaffected — "you control" only
        assertThat(oppGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new StirringHopesinger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }
    @Test
    @DisplayName("A sorcery targeting an opponent's creature triggers Repartee before the spell resolves")
    void sorceryTargetingOpposingCreatureTriggers() {
        Permanent hopesinger = harness.addToBattlefieldAndReturn(player1, new StirringHopesinger());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpittingEarth()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(hopesinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's instant targeting your creature does not trigger Repartee")
    void opponentsSpellDoesNotTrigger() {
        Permanent hopesinger = harness.addToBattlefieldAndReturn(player1, new StirringHopesinger());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hopesinger.getId());
        harness.passBothPriorities();

        assertThat(hopesinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Stirring Hopesinger");
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Repartee")
    void creatureSpellDoesNotTrigger() {
        Permanent hopesinger = harness.addToBattlefieldAndReturn(player1, new StirringHopesinger());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hopesinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Hill Giant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Repartee affects creatures present when the ability resolves")
    void creaturesEnteringAfterCastReceiveCounter() {
        Permanent hopesinger = harness.addToBattlefieldAndReturn(player1, new StirringHopesinger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, hopesinger.getId());
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hopesinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Stirring Hopesinger");
    }

    @Test
    @DisplayName("Each qualifying spell triggers Repartee independently")
    void multipleSpellsEachAddCounter() {
        Permanent hopesinger = harness.addToBattlefieldAndReturn(player1, new StirringHopesinger());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hopesinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Hill Giant");
    }
}
