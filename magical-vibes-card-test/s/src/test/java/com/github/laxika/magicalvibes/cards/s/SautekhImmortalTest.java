package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SautekhImmortal.class, GrizzlyBears.class, Shock.class})
class SautekhImmortalTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without counters when no creature died this turn")
    void entersWithNoCountersWhenNoDeaths() {
        castImmortal();

        assertThat(findImmortal().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with a counter for each creature that died this turn, any player")
    void entersWithCountersForAllDeaths() {
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);

        castImmortal();

        assertThat(findImmortal().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts a creature actually killed earlier in the turn")
    void entersWithCounterAfterActualDeath() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        castImmortal();

        assertThat(findImmortal().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flash permits casting during the opponent's end step")
    void canCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        castImmortal();

        harness.assertOnBattlefield(player1, "Sautekh Immortal");
        assertThat(findImmortal().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts a creature dying in response to the creature spell")
    void countsDeathWhileOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new SautekhImmortal()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Sautekh Immortal");

        harness.passBothPriorities();

        assertThat(findImmortal().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castImmortal() {
        harness.setHand(player1, List.of(new SautekhImmortal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findImmortal() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sautekh Immortal"))
                .findFirst().orElseThrow();
    }
}
