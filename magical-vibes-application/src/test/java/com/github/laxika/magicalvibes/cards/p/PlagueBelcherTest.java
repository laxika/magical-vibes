package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlagueBelcherTest extends BaseCardTest {

    // ===== ETB: two -1/-1 counters on target creature you control =====

    @Test
    @DisplayName("ETB puts two -1/-1 counters on a creature you control")
    void etbPutsTwoCountersOnControlledCreature() {
        // Air Elemental (4/4) survives two -1/-1 counters as a 2/2.
        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.setHand(player1, List.of(new PlagueBelcher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent target = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new PlagueBelcher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() ->
                harness.getGameService().playCard(gd, player1, 0, 0, opponentBearsId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    // ===== Death trigger: another Zombie you control dies =====

    @Test
    @DisplayName("Each opponent loses 1 life when another Zombie you control dies")
    void zombieDeathDrainsEachOpponent() {
        addPlagueBelcherReady(player1);
        harness.addToBattlefield(player1, new Gravedigger()); // 2/2 Zombie

        int p2LifeBefore = gd.getLife(player2.getId());

        // Opponent kills the Zombie with Shock.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID zombieId = harness.getPermanentId(player1, "Gravedigger");
        harness.castInstant(player2, 0, zombieId);
        harness.passBothPriorities(); // resolve Shock -> Zombie dies -> death trigger
        harness.passBothPriorities(); // resolve Plague Belcher's trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
    }

    @Test
    @DisplayName("Does NOT drain when a non-Zombie you control dies")
    void nonZombieDeathDoesNotDrain() {
        addPlagueBelcherReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 Bear, not a Zombie

        int p2LifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // resolve Shock -> Bear dies

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Plague Belcher's own death does not trigger (another Zombie only)")
    void ownDeathDoesNotDrain() {
        addPlagueBelcherReady(player1); // 5/4 Zombie

        int p2LifeBefore = gd.getLife(player2.getId());

        // Two Shocks (4 damage) kill the 5/4.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        UUID belcherId = harness.getPermanentId(player1, "Plague Belcher");
        harness.castInstant(player2, 0, belcherId);
        harness.passBothPriorities(); // 2 damage marked
        harness.castInstant(player2, 0, belcherId);
        harness.passBothPriorities(); // 4 damage -> dies to SBA

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plague Belcher"));
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    private void addPlagueBelcherReady(Player player) {
        Permanent perm = new Permanent(new PlagueBelcher());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }
}
