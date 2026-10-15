package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Geistflame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InquisitorsFlail.class, GrizzlyBears.class, SerraAngel.class, BenalishKnight.class, FurnaceOfRath.class, Geistflame.class})
class InquisitorsFlailTest extends BaseCardTest {

    @Test
    @DisplayName("Inquisitor's Flail has equip {2} ability")
    void hasEquipAbility() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent flail = addFlail(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isEqualTo(bear.getId());
        declareAttackers(player1, List.of(0));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Equipped creature deals double combat damage to player when unblocked")
    void doublesUnblockedCombatDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(bear.getId());

        declareAttackers(player1, List.of(0)); // bear at index 0

        // 2 combat damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Equipped attacker deals double combat damage to blocker")
    void doublesOutgoingDamageToBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // 4/4 blocker — base 2 damage wouldn't kill, but doubled 4 does
        addCreatureReady(player2, new SerraAngel()); // 4/4

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Serra Angel (4/4) takes 2*2=4 — exactly lethal
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Equipped creature receives double combat damage from blocker")
    void doublesIncomingDamageFromBlocker() {
        // 2/4 attacker with Flail
        GrizzlyBears creature2_4 = new GrizzlyBears();
        creature2_4.setPower(2);
        creature2_4.setToughness(4);
        Permanent attacker = addCreatureReady(player1, creature2_4);
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // 2/2 blocker — normally 2 damage to 2/4 (4 toughness), survives
        // But with Flail doubling incoming: 2*2=4, exactly lethal
        addCreatureReady(player2, new GrizzlyBears()); // 2/2

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 2/4 attacker takes 2*2=4 doubled incoming damage — exactly lethal
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Blocker takes 2*2=4 doubled outgoing damage — also dies
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Unattached Flail on battlefield does not double combat damage")
    void unattachedFlailDoesNotAffectCombat() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GrizzlyBears()); // 2/2
        addFlail(player1); // not attached

        declareAttackers(player1, List.of(0)); // bear at index 0

        // 2 combat damage — NOT doubled
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Equipped blocker deals and receives double combat damage")
    void equippedBlockerDoublesBothWays() {
        // 2/2 unequipped attacker
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        attacker.setAttacking(true);

        // 2/4 blocker with Flail
        GrizzlyBears creature2_4 = new GrizzlyBears();
        creature2_4.setPower(2);
        creature2_4.setToughness(4);
        Permanent blocker = addCreatureReady(player2, creature2_4);
        Permanent flail = addFlail(player2);
        flail.setAttachedTo(blocker.getId());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Blocker deals 2*2=4 to attacker (2/2) — attacker dies
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Blocker receives 2*2=4 incoming damage — exactly lethal for 4 toughness, dies
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Flail stacks with Furnace of Rath for outgoing combat damage")
    void stacksWithFurnaceOfRath() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FurnaceOfRath());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(bear.getId());

        declareAttackers(player1, List.of(1)); // Furnace at 0, bear at 1

        // 2 * 2 (Furnace global) * 2 (Flail source) = 8
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Removing Flail stops doubling combat damage")
    void removingFlailStopsDoubling() {
        harness.setLife(player2, 20);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(bear.getId());

        declareAttackers(player1, List.of(0)); // bear at index 0

        // 2 * 2 = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Detach Flail
        flail.setAttachedTo(null);

        // Attack again
        harness.setLife(player2, 20);
        bear.untap();
        declareAttackers(player1, List.of(0));

        // 2 damage — NOT doubled
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Flail does not double non-combat spell damage")
    void doesNotDoubleSpellDamage() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addFlail(player1).setAttachedTo(bear.getId());
        harness.setHand(player2, List.of(new Geistflame()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubled first-strike combat damage from equipped creature kills blocker before regular damage")
    void doublesFirstStrikeDamage() {
        // 2/2 first strike attacker with Flail
        Permanent attacker = addCreatureReady(player1, new BenalishKnight()); // 2/2 first strike
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // 3/3 blocker — first strike 2*2=4 >= 3, dies before dealing regular damage
        GrizzlyBears creature3_3 = new GrizzlyBears();
        creature3_3.setPower(3);
        creature3_3.setToughness(3);
        addCreatureReady(player2, creature3_3);

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 3/3 blocker takes 2*2=4 first-strike doubled damage — dies
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Knight survives since blocker died in first strike phase
        harness.assertOnBattlefield(player1, "Benalish Knight");
    }

    @Test
    @DisplayName("Two Flails multiply outgoing combat damage by four")
    void twoFlailsQuadrupleOutgoingDamage() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addFlail(player1).setAttachedTo(bear.getId());
        addFlail(player1).setAttachedTo(bear.getId());
        declareAttackers(player1, List.of(0));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Flails on both combatants double damage for source and recipient")
    void flailsOnBothCombatantsQuadrupleDamage() {
        Permanent attacker = addCreatureReady(player1, new SerraAngel());
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        addFlail(player1).setAttachedTo(attacker.getId());
        addFlail(player2).setAttachedTo(blocker.getId());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(attacker.getMarkedDamage()).isEqualTo(16);
        assertThat(blocker.getMarkedDamage()).isEqualTo(16);
    }

    private Permanent addFlail(Player player) {
        return harness.addToBattlefieldAndReturn(player, new InquisitorsFlail());
    }
}
