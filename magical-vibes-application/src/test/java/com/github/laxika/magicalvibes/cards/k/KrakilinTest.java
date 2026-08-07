package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KrakilinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters as a 3/3 with three +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new Krakilin()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent krakilin = findPermanent(player1, "Krakilin");
        assertThat(krakilin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(krakilin.getEffectivePower()).isEqualTo(3);
        assertThat(krakilin.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 enters as a 0/0 and dies to state-based actions")
    void xZeroDies() {
        harness.setHand(player1, List.of(new Krakilin()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Krakilin");
        harness.assertInGraveyard(player1, "Krakilin");
    }

    @Test
    @DisplayName("Activating {1}{G} grants a regeneration shield")
    void activationGrantsShield() {
        addKrakilinReady(player1, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent krakilin = findPermanent(player1, "Krakilin");
        assertThat(krakilin.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Krakilin from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent krakilin = addKrakilinReady(player1, 2);
        krakilin.setRegenerationShield(1);
        krakilin.setBlocking(true);
        krakilin.addBlockingTarget(0);

        Permanent attacker = addBearsReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Krakilin");
        Permanent survivor = findPermanent(player1, "Krakilin");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Krakilin dies to lethal combat damage without a shield")
    void diesWithoutShield() {
        Permanent krakilin = addKrakilinReady(player1, 2);
        krakilin.setBlocking(true);
        krakilin.addBlockingTarget(0);

        Permanent attacker = addBearsReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Krakilin");
        harness.assertInGraveyard(player1, "Krakilin");
    }

    private Permanent addKrakilinReady(Player player, int counters) {
        Permanent perm = new Permanent(new Krakilin());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBearsReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
