package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FreyaliseSupplicantTest extends BaseCardTest {

    @Test
    @DisplayName("Deals half the sacrificed creature's power to target player, rounded down")
    void dealsHalfPowerToPlayer() {
        addCreatureReady(player1, new FreyaliseSupplicant());
        addCreatureReady(player1, new SerraAngel()); // 4/4 white
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Rounds down: a 3-power creature deals only 1 damage")
    void roundsDown() {
        addCreatureReady(player1, new FreyaliseSupplicant());
        addCreatureReady(player1, new HillGiant()); // 3/3 red
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Uses the sacrificed creature's effective (boosted) power")
    void usesEffectivePower() {
        addCreatureReady(player1, new FreyaliseSupplicant());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // power 4
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage can be aimed at a creature")
    void dealsDamageToCreature() {
        addCreatureReady(player1, new FreyaliseSupplicant());
        addCreatureReady(player1, new SerraAngel()); // 4/4 -> 2 damage
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 victim
        UUID victim = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, victim);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage can be aimed at a planeswalker")
    void dealsDamageToPlaneswalker() {
        addCreatureReady(player1, new FreyaliseSupplicant());
        addCreatureReady(player1, new SerraAngel()); // 4/4 -> 2 damage
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chosen sacrifice picks between multiple red or white creatures")
    void choosesAmongEligibleCreatures() {
        addCreatureReady(player1, new FreyaliseSupplicant());
        addCreatureReady(player1, new SerraAngel()); // 4/4
        addCreatureReady(player1, new EliteVanguard()); // 2/1
        UUID vanguard = harness.getPermanentId(player1, "Elite Vanguard");
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, vanguard);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Elite Vanguard");
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Cannot activate without a red or white creature to sacrifice")
    void cannotActivateWithoutRedOrWhiteCreature() {
        addCreatureReady(player1, new FreyaliseSupplicant());
        addCreatureReady(player1, new GrizzlyBears()); // green, ineligible

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
