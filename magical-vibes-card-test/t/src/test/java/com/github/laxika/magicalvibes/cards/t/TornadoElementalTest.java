package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdvancedHoverguard;
import com.github.laxika.magicalvibes.cards.a.AuriokWindwalker;
import com.github.laxika.magicalvibes.cards.f.FangrenPathcutter;
import com.github.laxika.magicalvibes.cards.p.PlasmaElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TornadoElemental.class, AdvancedHoverguard.class, AuriokWindwalker.class,
        FangrenPathcutter.class, PlasmaElemental.class})
class TornadoElementalTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, deals 6 damage to each creature with flying")
    void dealsSixDamageToFlyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AdvancedHoverguard());
        harness.addToBattlefield(player2, new AuriokWindwalker());
        harness.setHand(player1, List.of(new TornadoElemental()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Advanced Hoverguard");
        harness.assertNotOnBattlefield(player2, "Auriok Windwalker");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("When it enters, does not damage creatures without flying")
    void doesNotDamageNonFlyingCreatures() {
        harness.addToBattlefield(player2, new PlasmaElemental());
        harness.setHand(player1, List.of(new TornadoElemental()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Plasma Elemental");
    }

    @Test
    @DisplayName("Blocked Tornado Elemental can assign combat damage to defending player")
    void blockedTornadoElementalAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new TornadoElemental());
        Permanent blocker = addCreatureReady(player2, new FangrenPathcutter());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 6));

        harness.assertLife(player2, 14);
        harness.assertOnBattlefield(player2, "Fangren Pathcutter");
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Blocked Tornado Elemental may assign combat damage to its blocker")
    void blockedTornadoElementalAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new TornadoElemental());
        Permanent blocker = addCreatureReady(player2, new FangrenPathcutter());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Fangren Pathcutter");
        harness.assertInGraveyard(player2, "Fangren Pathcutter");
    }

    @Test
    @DisplayName("Damage cannot be split between a blocker and the defending player")
    void cannotSplitDamageBetweenBlockerAndPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new TornadoElemental());
        Permanent blocker = addCreatureReady(player2, new FangrenPathcutter());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 3, player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 6));
        harness.assertLife(player2, 14);
        harness.assertOnBattlefield(player2, "Fangren Pathcutter");
    }

    @Test
    @DisplayName("Assigning damage as though unblocked remains optional after blockers leave combat")
    void offersChoiceWhenBlockedWithoutRemainingBlockers() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new TornadoElemental());
        attacker.setAttacking(true);
        attacker.setBlockedWithoutBlockers(true);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
