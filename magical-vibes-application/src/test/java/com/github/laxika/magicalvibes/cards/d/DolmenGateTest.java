package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DolmenGate.class, GrizzlyBears.class, Shock.class, Naturalize.class, SongOfTheDryads.class})
class DolmenGateTest extends BaseCardTest {

    private Permanent addAttacker(Player controller) {
        Permanent attacker = addCreatureReady(controller, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker(Player controller, Permanent attacker) {
        Permanent blocker = addCreatureReady(controller, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        return blocker;
    }

    @Test
    @DisplayName("Combat damage to an attacking creature you control is prevented")
    void preventsCombatDamageToYourAttacker() {
        harness.addToBattlefield(player1, new DolmenGate());
        Permanent attacker = addAttacker(player1);
        Permanent blocker = addBlocker(player2, attacker);

        resolveCombat();

        // Attacker takes no combat damage from its blocker — survives with no marked damage.
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage to a blocking creature you control is not prevented (only attackers)")
    void doesNotPreventDamageToYourBlocker() {
        harness.addToBattlefield(player1, new DolmenGate());
        Permanent attacker = addAttacker(player2);
        Permanent blocker = addBlocker(player1, attacker);

        resolveCombat(player2);

        // The blocker is not attacking, so it receives the attacker's 2 combat damage normally.
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage to your attacking creature is not prevented")
    void doesNotPreventNoncombatDamage() {
        harness.addToBattlefield(player1, new DolmenGate());
        Permanent attacker = addAttacker(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        // Shock is noncombat damage — Dolmen Gate does not prevent it.
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Dolmen Gate does not protect an opponent's attacking creature")
    void doesNotProtectOpponentsAttacker() {
        harness.addToBattlefield(player1, new DolmenGate());
        Permanent attacker = addAttacker(player2);
        addBlocker(player1, attacker);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A tapped Dolmen Gate protects every attacking creature you control")
    void tappedGateProtectsMultipleAttackers() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new DolmenGate());
        gate.tap();
        Permanent firstAttacker = addAttacker(player1);
        Permanent secondAttacker = addAttacker(player1);
        addBlocker(player2, firstAttacker);
        addBlocker(player2, secondAttacker);

        resolveCombat();

        assertThat(firstAttacker.getMarkedDamage()).isZero();
        assertThat(secondAttacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstAttacker, secondAttacker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Removing Dolmen Gate before combat damage ends its protection")
    void removingGateBeforeDamageEndsProtection() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new DolmenGate());
        Permanent attacker = addAttacker(player1);
        addBlocker(player2, attacker);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, gate.getId());
        harness.passBothPriorities();
        resolveCombat();

        harness.assertInGraveyard(player1, "Dolmen Gate");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dolmen Gate enchanted by Song of the Dryads no longer prevents combat damage")
    void gateThatLostItsPrintedAbilitiesDoesNotProtectAttackers() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new DolmenGate());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, gate.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Song of the Dryads").getAttachedTo()).isEqualTo(gate.getId());

        Permanent attacker = addAttacker(player1);
        addBlocker(player2, attacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gate).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
