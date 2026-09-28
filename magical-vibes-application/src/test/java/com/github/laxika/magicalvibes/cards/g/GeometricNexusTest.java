package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TellingTime;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeometricNexus.class, TellingTime.class, GrizzlyBears.class})
class GeometricNexusTest extends BaseCardTest {

    @Test
    @DisplayName("Adds charge counters equal to the mana value of any player's instant or sorcery")
    void addsCountersForAnyPlayersInstantOrSorcery() {
        Permanent nexus = addNexus(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new TellingTime()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(nexus.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for creature spells")
    void doesNotTriggerForCreatureSpells() {
        Permanent nexus = addNexus(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(nexus.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Creates a Fractal with one +1/+1 counter per removed charge counter")
    void createsFractalFromChargeCounters() {
        Permanent nexus = addNexus(player1);
        nexus.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);

        assertThat(nexus.getCounterCount(CounterType.CHARGE)).isZero();

        harness.passBothPriorities();

        Permanent fractal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Fractal".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(fractal.getEffectivePower()).isEqualTo(3);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addNexus(Player player) {
        Permanent nexus = harness.addToBattlefieldAndReturn(player, new GeometricNexus());
        nexus.setSummoningSick(false);
        return nexus;
    }
}
