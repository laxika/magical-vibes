package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Cultivate;
import com.github.laxika.magicalvibes.cards.t.TellingTime;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeometricNexus.class, TellingTime.class, GrizzlyBears.class, Cultivate.class, Cancel.class})
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

    @Test
    @DisplayName("Controller's sorcery adds charge counters before the spell resolves")
    void addsCountersForControllersSorcery() {
        Permanent nexus = addNexus(player1);
        nexus.setCounterCount(CounterType.CHARGE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Cultivate()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(nexus.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate without charge counters and the zero toughness token dies")
    void canActivateWithoutChargeCounters() {
        Permanent nexus = addNexus(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThat(nexus.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fractal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fractal uses the counters paid even if new counters are added before resolution")
    void usesCounterCountAtActivation() {
        Permanent nexus = addNexus(player1);
        nexus.setCounterCount(CounterType.CHARGE, 3);
        nexus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThat(nexus.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(nexus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        nexus.setCounterCount(CounterType.CHARGE, 5);
        harness.passBothPriorities();

        Permanent fractal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Fractal".equals(permanent.getCard().getName()))
                .findFirst().orElseThrow();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(nexus.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
    }


    @Test
    @DisplayName("Uses the mana value of a spell countered before the ability resolves")
    void usesManaValueOfSpellCounteredBeforeResolution() {
        Permanent nexus = addNexus(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        TellingTime tellingTime = new TellingTime();
        harness.setHand(player2, List.of(tellingTime));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0);
        harness.castInstant(player1, 0, tellingTime.getId());
        // Cancel is an instant too: its own trigger resolves first, then Cancel counters Telling Time.
        harness.passBothPriorities();
        assertThat(nexus.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(tellingTime);
        harness.passBothPriorities();

        assertThat(nexus.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
    }

    private Permanent addNexus(Player player) {
        Permanent nexus = harness.addToBattlefieldAndReturn(player, new GeometricNexus());
        nexus.setSummoningSick(false);
        return nexus;
    }
}
