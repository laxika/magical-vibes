package com.github.laxika.magicalvibes.service.combat.attack;

import com.github.laxika.magicalvibes.cards.a.AngelicArbiter;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FormOfTheDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LightOfDay;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.t.TrainingDrone;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec for {@link AttackLegalityService}, the creature-level half of declare-attackers legality
 * (CR 508.1a): may this permanent be declared as an attacker at all, may it be declared against a
 * given defender, and how many "attacks if able" requirements does it carry. The group-level half
 * — "can't attack alone", banding, satisfying as many requirements as possible — is enforced when
 * a declaration is submitted and belongs to {@code CombatAttackService}.
 *
 * <p>Each test pins one clause of the gate, so a clause that silently stops being consulted fails
 * here rather than only surfacing as a wrong list of offered attackers deep inside
 * {@code CombatAttackService.getAttackableCreatureIndices} or the AI's attack search.
 */
class AttackLegalityServiceTest extends BaseCardTest {

    @Test
    @DisplayName("An untapped creature that has been under its controller's control can attack")
    void readyCreatureCanAttack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(als.canAttack(gd, bears, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("A tapped creature can't attack")
    void tappedCreatureCannotAttack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("CR 302.6: a summoning-sick creature can't attack")
    void summoningSickCreatureCannotAttack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isSummoningSick()).isTrue();

        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("A non-creature permanent can't attack")
    void nonCreaturePermanentCannotAttack() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = findPermanent(player1, "Forest");

        assertThat(als.canAttack(gd, forest, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("CR 702.3b: a creature with defender can't attack")
    void defenderStopsAttacking() {
        Permanent wall = addCreatureReady(player1, new WallOfWood());

        assertThat(als.canAttack(gd, wall, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("The \"can't attack this turn\" flag stops attacking")
    void cantAttackThisTurnFlagStopsAttacking() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCantAttackThisTurn(true);

        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("An aura granting \"can't attack or block\" stops the enchanted creature attacking")
    void auraCantAttackOrBlockStopsAttacking() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player1, new Pacifism());

        assertThat(als.canAttack(gd, bears, player1.getId())).isTrue();

        pacifism.setAttachedTo(bears.getId());
        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("\"Can't attack or block unless equipped\" is gated on an attached Equipment")
    void equipmentRequirementGatesAttacking() {
        Permanent drone = addCreatureReady(player1, new TrainingDrone());
        Permanent scimitar = addCreatureReady(player1, new LeoninScimitar());

        // On the battlefield but attached to nothing — the requirement is not met.
        assertThat(als.canAttack(gd, drone, player1.getId())).isFalse();

        scimitar.setAttachedTo(drone.getId());
        assertThat(als.canAttack(gd, drone, player1.getId())).isTrue();

        scimitar.setAttachedTo(null);
        assertThat(als.canAttack(gd, drone, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("A board-wide \"can't attack or block\" restriction applies to matching creatures only")
    void globalCantAttackOrBlockRestrictionStopsAttacking() {
        // Light of Day: "Black creatures can't attack or block."
        Permanent zombies = addCreatureReady(player1, new ScatheZombies());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(als.canAttack(gd, zombies, player1.getId())).isTrue();

        harness.addToBattlefield(player2, new LightOfDay());

        assertThat(als.canAttack(gd, zombies, player1.getId())).isFalse();
        assertThat(als.canAttack(gd, bears, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Angelic Arbiter stops a player who cast a spell this turn from attacking at all")
    void castingASpellCanLockThePlayerOutOfAttacking() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AngelicArbiter());

        assertThat(als.isPlayerPreventedFromAttacking(gd, player1.getId())).isFalse();

        gd.recordSpellCast(player1.getId(), new GrizzlyBears());

        assertThat(als.isPlayerPreventedFromAttacking(gd, player1.getId())).isTrue();
        // The restriction is the attacking player's own; their opponent is unaffected.
        assertThat(als.isPlayerPreventedFromAttacking(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("A defender-scoped restriction only bars attackers that fail its exemption")
    void defenderScopedRestrictionBarsNonExemptAttackers() {
        // Form of the Dragon: "Creatures without flying can't attack you."
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent drake = addCreatureReady(player1, new WindDrake());

        assertThat(als.canAttackDefender(gd, bears, player2.getId())).isTrue();

        harness.addToBattlefield(player2, new FormOfTheDragon());

        assertThat(als.canAttackDefender(gd, bears, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, drake, player2.getId())).isTrue();
        // The restriction protects only its controller — attacking its owner's opponent is fine.
        assertThat(als.canAttackDefender(gd, bears, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Attack requirements are counted, not just detected")
    void mustAttackRequirementsAreCounted() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent berserkers = addCreatureReady(player1, new BerserkersOfBloodRidge());

        assertThat(als.getMustAttackRequirementCount(gd, bears)).isZero();
        assertThat(als.getMustAttackRequirementCount(gd, berserkers)).isEqualTo(1);

        // A transient "must attack this turn" stacks with the printed requirement rather than
        // replacing it — CR 508.1d compares totals, so two requirements must outrank one.
        berserkers.setMustAttackThisTurn(true);
        assertThat(als.getMustAttackRequirementCount(gd, berserkers)).isEqualTo(2);
    }

    @Test
    @DisplayName("The defending player is always a legal attack target")
    void defendingPlayerIsAValidAttackTarget() {
        assertThat(als.getValidAttackTargetIds(gd, player1.getId())).contains(player2.getId());
        assertThat(als.buildAvailableTargets(gd, player1.getId()))
                .anyMatch(target -> target.id().equals(player2.getId()) && target.isPlayer());
    }
}
