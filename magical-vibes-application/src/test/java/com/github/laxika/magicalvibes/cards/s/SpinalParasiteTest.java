package com.github.laxika.magicalvibes.cards.s;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinalParasite.class, StaffOfDomination.class})
class SpinalParasiteTest extends BaseCardTest {

    @Test
    @DisplayName("Sunburst puts one +1/+1 counter on Spinal Parasite for each color spent")
    void sunburstCountsDistinctColors() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpinalParasite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent parasite = findPermanent(player1, "Spinal Parasite");
        assertThat(parasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Sunburst counts a repeated color only once")
    void sunburstCountsEachColorOnlyOnce() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpinalParasite()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent parasite = findPermanent(player1, "Spinal Parasite");
        assertThat(parasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sunburst ignores colorless mana")
    void sunburstIgnoresColorlessMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpinalParasite()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spinal Parasite");
        harness.assertInGraveyard(player1, "Spinal Parasite");
    }

    @Test
    @DisplayName("Activated ability removes two +1/+1 counters and a counter from the target")
    void removesCounters() {
        Permanent parasite = addReadyParasite();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StaffOfDomination());
        target.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(parasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Activated ability can target a permanent without counters")
    void canTargetPermanentWithoutCounters() {
        Permanent parasite = addReadyParasite();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StaffOfDomination());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(parasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Activated ability requires two +1/+1 counters")
    void requiresTwoPlusOneCounters() {
        Permanent parasite = addReadyParasite();
        parasite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StaffOfDomination());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent addReadyParasite() {
        Permanent parasite = addCreatureReady(player1, new SpinalParasite());
        parasite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        return parasite;
    }
}
