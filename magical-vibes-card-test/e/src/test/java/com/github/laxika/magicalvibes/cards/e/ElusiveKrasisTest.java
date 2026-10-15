package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Crocanura;
import com.github.laxika.magicalvibes.cards.l.LeylinePhantom;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElusiveKrasis.class, Crocanura.class, LeylinePhantom.class})
class ElusiveKrasisTest extends BaseCardTest {

    @Test
    @DisplayName("Elusive Krasis cannot be blocked")
    void cannotBeBlocked() {
        harness.addToBattlefield(player2, new Crocanura());

        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new ElusiveKrasis());
        krasis.setSummoningSick(false);
        krasis.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on when a creature with greater power enters")
    void evolvesForGreaterPowerCreature() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new ElusiveKrasis());

        harness.setHand(player1, List.of(new Crocanura()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger for a creature with equal power and toughness")
    void doesNotEvolveForSmallerCreature() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new ElusiveKrasis());

        harness.setHand(player1, List.of(new ElusiveKrasis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void evolvesForGreaterToughnessWithEqualPower() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new ElusiveKrasis());
        krasis.setPowerModifier(5);

        harness.setHand(player1, List.of(new LeylinePhantom()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForOpponentsCreature() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new ElusiveKrasis());

        harness.enterBattlefieldAndReturn(player2, new Crocanura());
        resolveAllTriggers();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rechecksConditionAfterAnotherEvolveTriggerResolves() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new ElusiveKrasis());

        harness.enterBattlefieldAndReturn(player1, new Crocanura());
        harness.enterBattlefieldAndReturn(player1, new Crocanura());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
