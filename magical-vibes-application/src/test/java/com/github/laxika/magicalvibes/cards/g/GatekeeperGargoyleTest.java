package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IzzetGuildgate;
import com.github.laxika.magicalvibes.cards.s.SteamVents;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GatekeeperGargoyle.class, IzzetGuildgate.class, SteamVents.class})
class GatekeeperGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each Gate its controller controls")
    void entersWithCountersForControlledGates() {
        harness.addToBattlefield(player1, new IzzetGuildgate());
        harness.addToBattlefield(player1, new IzzetGuildgate());

        castGargoyle();

        Permanent gargoyle = findPermanent(player1, "Gatekeeper Gargoyle");
        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count Gates controlled by an opponent")
    void doesNotCountOpponentsGates() {
        harness.addToBattlefield(player2, new IzzetGuildgate());

        castGargoyle();

        Permanent gargoyle = findPermanent(player1, "Gatekeeper Gargoyle");
        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Non-Gate lands and Gates outside the battlefield do not supply counters")
    void ignoresNonGatesAndGatesOutsideBattlefield() {
        harness.addToBattlefield(player1, new SteamVents());
        harness.setGraveyard(player1, List.of(new IzzetGuildgate()));
        harness.setHand(player2, List.of(new IzzetGuildgate()));

        castGargoyle();

        assertThat(findPermanent(player1, "Gatekeeper Gargoyle")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts Gates when entering rather than when cast")
    void countsGatesAtEntry() {
        Permanent departedGate = harness.addToBattlefieldAndReturn(player1, new IzzetGuildgate());
        harness.castFromHand(player1, new GatekeeperGargoyle(), "{6}");
        gd.playerBattlefields.get(player1.getId()).remove(departedGate);
        harness.setGraveyard(player1, List.of(departedGate.getCard()));
        harness.addToBattlefield(player1, new IzzetGuildgate());
        harness.addToBattlefield(player1, new IzzetGuildgate());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gatekeeper Gargoyle")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters remain fixed when the number of Gates changes after entry")
    void countersDoNotTrackLaterGateChanges() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new IzzetGuildgate());
        castGargoyle();
        Permanent gargoyle = findPermanent(player1, "Gatekeeper Gargoyle");
        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(gate);
        harness.setGraveyard(player1, List.of(gate.getCard()));
        harness.runStateBasedActions();
        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.addToBattlefield(player1, new IzzetGuildgate());
        harness.addToBattlefield(player1, new IzzetGuildgate());
        harness.runStateBasedActions();
        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Also enters with counters when entering without being cast")
    void entersWithCountersWithoutBeingCast() {
        harness.addToBattlefield(player1, new IzzetGuildgate());
        harness.addToBattlefield(player2, new IzzetGuildgate());

        Permanent gargoyle = harness.enterBattlefieldAndReturn(player1, new GatekeeperGargoyle());

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castGargoyle() {
        harness.castFromHand(player1, new GatekeeperGargoyle(), "{6}");
        harness.passBothPriorities();
    }
}
