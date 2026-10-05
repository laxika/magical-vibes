package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlagueBelcher.class, AirElemental.class, Gravedigger.class, GrizzlyBears.class,
        Shock.class, WrathOfGod.class, AmoeboidChangeling.class})
class PlagueBelcherTest extends BaseCardTest {

    // ===== ETB: two -1/-1 counters on target creature you control =====

    @Test
    @DisplayName("ETB puts two -1/-1 counters on a creature you control")
    void etbPutsTwoCountersOnControlledCreature() {
        // Air Elemental (4/4) survives two -1/-1 counters as a 2/2.
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new PlagueBelcher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        UUID opponentBearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new PlagueBelcher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() ->
                harness.castCreature(player1, 0, opponentBearsId))
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

        harness.assertNotOnBattlefield(player1, "Plague Belcher");
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("ETB can target Plague Belcher itself on an otherwise empty battlefield")
    void etbCanTargetItself() {
        harness.setHand(player1, List.of(new PlagueBelcher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent belcher = findPermanent(player1, "Plague Belcher");
        harness.handlePermanentChosen(player1, belcher.getId());
        harness.passBothPriorities();

        assertThat(belcher.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, belcher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, belcher)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A Zombie killed by the ETB counters triggers the life loss ability")
    void etbKillingAnotherZombieDrainsOpponent() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravedigger());
        harness.setHand(player1, List.of(new PlagueBelcher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, zombie.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gravedigger");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opposing Zombie dying does not trigger life loss")
    void opposingZombieDeathDoesNotDrain() {
        addPlagueBelcherReady(player1);
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new Gravedigger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, zombie.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gravedigger");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Simultaneous deaths trigger once per other allied Zombie even when Plague Belcher dies")
    void simultaneousDeathsDrainForEachOtherAlliedZombie() {
        addPlagueBelcherReady(player1);
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Gravedigger());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plague Belcher");
        harness.assertNotOnBattlefield(player1, "Gravedigger");
        harness.assertNotOnBattlefield(player2, "Gravedigger");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An ETB target removed in response does not move its counters onto Plague Belcher")
    void removedEtbTargetDoesNotRedirectCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlagueBelcher()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Plague Belcher")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A creature that gained the Zombie type triggers life loss when it dies")
    void gainedZombieTypeCountsAtDeath() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new AmoeboidChangeling());
        changeling.setSummoningSick(false);
        addPlagueBelcherReady(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ZOMBIE)).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A Zombie that lost its creature types does not trigger life loss when it dies")
    void lostZombieTypeDoesNotCountAtDeath() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new AmoeboidChangeling());
        changeling.setSummoningSick(false);
        addPlagueBelcherReady(player1);
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravedigger());

        harness.activateAbility(player1, 0, 1, null, zombie.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasEffectiveSubtype(gd, zombie, CardSubtype.ZOMBIE)).isFalse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, zombie.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Gravedigger");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    private void addPlagueBelcherReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PlagueBelcher());
        perm.setSummoningSick(false);
    }
}
