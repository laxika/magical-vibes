package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Statecraft.class, FreshVolunteers.class, Sizzle.class, Tremor.class,
        MinimusContainment.class, Disenchant.class, SelflessSquire.class, HealingSalve.class})
class StatecraftTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage to and by creatures you control")
    void preventsCombatDamageToAndByYourCreatures() {
        harness.addToBattlefield(player1, new Statecraft());
        Permanent blocker = addCreatureReady(player1, new FreshVolunteers());
        Permanent attacker = addAttacker(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage dealt by your creatures to players")
    void preventsYourCreaturesFromDealingCombatDamage() {
        harness.addToBattlefield(player1, new Statecraft());
        harness.setLife(player2, 20);
        addAttacker(player1);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent combat damage from creatures you do not control")
    void doesNotPreventOpponentsCombatDamage() {
        harness.addToBattlefield(player1, new Statecraft());
        harness.setLife(player1, 20);
        addAttacker(player2);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.addToBattlefield(player1, new Statecraft());
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new Sizzle(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage to creatures you control")
    void doesNotPreventNoncombatDamageToYourCreatures() {
        harness.addToBattlefield(player1, new Statecraft());
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Stops preventing combat damage when Statecraft loses its abilities")
    void stopsPreventingCombatDamageWhenItLosesItsAbilities() {
        Permanent statecraft = harness.addToBattlefieldAndReturn(player1, new Statecraft());
        harness.setHand(player1, List.of(new MinimusContainment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, statecraft.getId());
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        addAttacker(player1);
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Stops preventing combat damage when Statecraft leaves the battlefield")
    void stopsPreventingCombatDamageWhenDestroyed() {
        Permanent statecraft = harness.addToBattlefieldAndReturn(player1, new Statecraft());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, statecraft.getId());

        harness.setLife(player2, 20);
        addAttacker(player1);
        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(statecraft);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevention follows Statecraft's current controller")
    void preventionFollowsCurrentController() {
        Permanent statecraft = harness.addToBattlefieldAndReturn(player1, new Statecraft());
        gd.playerBattlefields.get(player1.getId()).remove(statecraft);
        gd.playerBattlefields.get(player2.getId()).add(statecraft);
        harness.setLife(player1, 20);
        addAttacker(player2);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Former controller's creatures can deal damage after Statecraft changes control")
    void formerControllerCreaturesAreNotProtected() {
        Permanent statecraft = harness.addToBattlefieldAndReturn(player1, new Statecraft());
        gd.playerBattlefields.get(player1.getId()).remove(statecraft);
        gd.playerBattlefields.get(player2.getId()).add(statecraft);
        harness.setLife(player2, 20);
        addAttacker(player1);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Combat damage it prevents to a player is damage prevented to that player")
    void preventedCombatDamageToAPlayerIsSeenAsPrevented() {
        harness.addToBattlefield(player1, new Statecraft());
        Permanent squire = harness.addToBattlefieldAndReturn(player2, new SelflessSquire());
        harness.setLife(player2, 20);
        addAttacker(player1);

        resolveCombat(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The damaged player orders its prevention of their creature's damage against their own shield")
    void damagedPlayerOrdersItAgainstTheirShield() {
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Statecraft());
        harness.setLife(player2, 20);
        addAttacker(player1);

        resolveCombat(player1);

        var order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        assertThat(order.playerId()).isEqualTo(player2.getId());
        String statecraftOption = order.options().stream()
                .filter(option -> option.contains("Prevent the damage to you")).findFirst().orElseThrow();
        harness.handleListChoice(player2, statecraftOption);

        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields().getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Its controller still divides the combat damage of a creature blocked by two creatures")
    void preventedAttackerStillAssignsItsDamage() {
        harness.addToBattlefield(player1, new Statecraft());
        addCreatureReady(player1, new FreshVolunteers());
        Permanent firstBlocker = addCreatureReady(player2, new FreshVolunteers());
        Permanent secondBlocker = addCreatureReady(player2, new FreshVolunteers());

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));
        resolveCombat(player1);

        var assignment = gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(assignment).isNotNull();
        assertThat(assignment.playerId()).isEqualTo(player1.getId());
        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(secondBlocker.getMarkedDamage()).isZero();
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new FreshVolunteers());
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.getId().equals(player1.getId()) ? player2.getId() : player1.getId());
        return attacker;
    }
}
