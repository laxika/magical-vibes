package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatekeeperGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each Gate its controller controls")
    void entersWithCountersForControlledGates() {
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());

        castGargoyle();

        Permanent gargoyle = findPermanent(player1, "Gatekeeper Gargoyle");
        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count Gates controlled by an opponent")
    void doesNotCountOpponentsGates() {
        harness.addToBattlefield(player2, new RakdosGuildgate());

        castGargoyle();

        Permanent gargoyle = findPermanent(player1, "Gatekeeper Gargoyle");
        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castGargoyle() {
        harness.setHand(player1, List.of(new GatekeeperGargoyle()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
