package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        castImmortal();

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
        return findImmortal(player1);
    }

    private Permanent findImmortal(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sautekh Immortal"))
                .findFirst().orElseThrow();
    }
}
