package com.github.laxika.magicalvibes.service.combat.attack;

import com.github.laxika.magicalvibes.cards.a.AngelicArbiter;
import com.github.laxika.magicalvibes.cards.a.AnimateWall;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.ChaosLord;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.c.CurseOfTheNightlyHunt;
import com.github.laxika.magicalvibes.cards.e.EnsnaringBridge;
import com.github.laxika.magicalvibes.cards.e.EvilEyeOfOrmsByGore;
import com.github.laxika.magicalvibes.cards.f.ForcedWorship;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FormOfTheDragon;
import com.github.laxika.magicalvibes.cards.g.GoblinAssault;
import com.github.laxika.magicalvibes.cards.g.GoblinRabblemaster;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InstillEnergy;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LightOfDay;
import com.github.laxika.magicalvibes.cards.o.Okk;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RollingStones;
import com.github.laxika.magicalvibes.cards.s.SandwurmConvergence;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.SeaSerpent;
import com.github.laxika.magicalvibes.cards.s.StormtideLeviathan;
import com.github.laxika.magicalvibes.cards.t.TrainingDrone;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
    @DisplayName("An aura granting \"attacks as though it had haste\" lifts summoning sickness")
    void hasteAuraLetsASummoningSickCreatureAttack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent instillEnergy = harness.addToBattlefieldAndReturn(player1, new InstillEnergy());

        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();

        instillEnergy.setAttachedTo(bears.getId());
        assertThat(als.canAttack(gd, bears, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("A printed \"attacks as though it had haste unless it entered this turn\" permission lifts summoning sickness only after the turn it entered")
    void printedHastePermissionIsGatedOnTheTurnItEntered() {
        harness.addToBattlefield(player1, new ChaosLord());
        Permanent chaosLord = findPermanent(player1, "Chaos Lord");
        assertThat(chaosLord.isSummoningSick()).isTrue();

        assertThat(als.canAttack(gd, chaosLord, player1.getId())).isTrue();

        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), id -> new ArrayList<>())
                .add(chaosLord.getCard());
        assertThat(als.canAttack(gd, chaosLord, player1.getId())).isFalse();
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
    @DisplayName("An aura granting \"can attack as though it didn't have defender\" lifts defender")
    void anAuraCanLiftDefender() {
        // Animate Wall: the enchanted Wall can attack as though it didn't have defender.
        Permanent wall = addCreatureReady(player1, new WallOfWood());
        Permanent animateWall = harness.addToBattlefieldAndReturn(player1, new AnimateWall());

        assertThat(als.canAttack(gd, wall, player1.getId())).isFalse();

        animateWall.setAttachedTo(wall.getId());
        assertThat(als.canAttack(gd, wall, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("A board-wide permission lifts defender for every creature it matches")
    void aBoardWidePermissionCanLiftDefender() {
        // Rolling Stones: "Wall creatures can attack as though they didn't have defender."
        Permanent wall = addCreatureReady(player1, new WallOfWood());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(als.canAttack(gd, wall, player1.getId())).isFalse();

        harness.addToBattlefield(player1, new RollingStones());

        assertThat(als.canAttack(gd, wall, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, bears, player1.getId())).isTrue();
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
    @DisplayName("An aura granting \"can't attack\" leaves the enchanted creature able to block")
    void auraCantAttackLeavesBlockingAlone() {
        // Forced Worship stops attacking only, where Pacifism stops both.
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forcedWorship = harness.addToBattlefieldAndReturn(player1, new ForcedWorship());
        forcedWorship.setAttachedTo(bears.getId());

        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();
        assertThat(bls.canBlock(gd, bears)).isTrue();
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
    @DisplayName("CR 508.1a: \"can't attack unless …\" is re-evaluated against the current board")
    void cantAttackUnlessConditionGatesAttacking() {
        // Sea Serpent: "can't attack unless defending player controls an Island".
        Permanent serpent = addCreatureReady(player1, new SeaSerpent());
        harness.addToBattlefield(player1, new Island());

        // An Island its own controller owns is not one the *defending* player controls.
        assertThat(als.canAttack(gd, serpent, player1.getId())).isFalse();

        harness.addToBattlefield(player2, new Island());
        assertThat(als.canAttack(gd, serpent, player1.getId())).isTrue();
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
    @DisplayName("A board-wide \"can't attack unless …\" restriction reaches both players' creatures")
    void globalCantAttackUnlessRestrictionReachesBothSides() {
        // Stormtide Leviathan: "Creatures without flying or islandwalk can't attack."
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent drake = addCreatureReady(player1, new WindDrake());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        harness.addToBattlefield(player2, new StormtideLeviathan());

        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();
        assertThat(als.canAttack(gd, drake, player1.getId())).isTrue();
        // The restriction is not controller-scoped — it binds its own controller's creatures too.
        assertThat(als.canAttack(gd, opposingBears, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("A controller-scoped restriction binds only its own controller's creatures")
    void controllerScopedRestrictionSparesTheOpponent() {
        // Evil Eye of Orms-by-Gore: "Creatures you control other than Eyes can't attack."
        Permanent eye = addCreatureReady(player1, new EvilEyeOfOrmsByGore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(als.canAttack(gd, eye, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();
        assertThat(als.canAttack(gd, opposingBears, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("A power-threshold restriction tracks the amount it is written against")
    void powerThresholdRestrictionTracksItsDynamicAmount() {
        // Ensnaring Bridge: "Creatures with power greater than the number of cards in your hand
        // can't attack" — the threshold is its controller's hand, re-counted at declaration time.
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent wurm = addCreatureReady(player1, new CrawWurm());
        harness.addToBattlefield(player2, new EnsnaringBridge());

        harness.setHand(player2, List.of());
        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();
        assertThat(als.canAttack(gd, wurm, player1.getId())).isFalse();

        // Craw Wurm is power 6, Grizzly Bears power 2 — a hand of three lets only the smaller through.
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        assertThat(als.canAttack(gd, bears, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, wurm, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("\"Other creatures can't attack this turn\" spares only the creatures it exempted")
    void otherCreaturesCantAttackLockSparesOnlyTheExemptCreature() {
        // Intimidation Bolt: "Creatures other than the targeted one can't attack this turn."
        Permanent spared = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new HillGiant());

        assertThat(als.canAttack(gd, other, player1.getId())).isTrue();

        gd.otherCreaturesCantAttackExemptCreatureIds.add(spared.getId());

        assertThat(als.canAttack(gd, spared, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, other, player1.getId())).isFalse();
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
    @DisplayName("A restriction that also covers planeswalkers is the only one a planeswalker feels")
    void onlyPlaneswalkerProtectingRestrictionsCoverPlaneswalkers() {
        // Form of the Dragon protects its controller alone; Sandwurm Convergence names
        // "you or planeswalkers you control", so only the latter shields the planeswalker.
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        harness.addToBattlefield(player2, new FormOfTheDragon());

        assertThat(als.canAttackDefender(gd, bears, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, bears, chandra.getId())).isTrue();
    }

    @Test
    @DisplayName("Sandwurm Convergence shields its controller's planeswalkers as well as its controller")
    void planeswalkerProtectingRestrictionCoversBoth() {
        Permanent drake = addCreatureReady(player1, new WindDrake());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        harness.addToBattlefield(player2, new SandwurmConvergence());

        assertThat(als.canAttackDefender(gd, drake, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, drake, chandra.getId())).isFalse();
        // Creatures without flying are exempt and may attack either.
        assertThat(als.canAttackDefender(gd, bears, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, bears, chandra.getId())).isTrue();
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
    @DisplayName("Requirements imposed from off the creature are counted alongside its printed ones")
    void requirementsFromElsewhereAreCountedToo() {
        // Okk is a Goblin, so Goblin Assault's board-wide requirement finds it.
        Permanent okk = addCreatureReady(player1, new Okk());

        assertThat(als.getMustAttackRequirementCount(gd, okk)).isZero();

        harness.addToBattlefield(player2, new GoblinAssault());
        assertThat(als.getMustAttackRequirementCount(gd, okk)).isEqualTo(1);

        // A taunt adds a second requirement, but only while the taunter is attackable.
        gd.tauntedThisTurn.put(player1.getId(), player2.getId());
        assertThat(als.getMustAttackRequirementCount(gd, okk)).isEqualTo(2);
    }

    @Test
    @DisplayName("A source-relative matcher only forces the source controller's other matching creatures")
    void sourceRelativeMatcherIsScopedToTheSourceController() {
        // Goblin Rabblemaster's matcher is "other Goblin creatures you control", so it must see the
        // source permanent's card id and controller when it is evaluated.
        Permanent ownGoblin = addCreatureReady(player1, new Okk());
        Permanent opponentGoblin = addCreatureReady(player2, new Okk());
        Permanent rabblemaster = harness.addToBattlefieldAndReturn(player1, new GoblinRabblemaster());

        assertThat(als.getMustAttackRequirementCount(gd, ownGoblin)).isEqualTo(1);
        assertThat(als.getMustAttackRequirementCount(gd, opponentGoblin)).isZero();
        assertThat(als.getMustAttackRequirementCount(gd, rabblemaster)).isZero();
    }

    @Test
    @DisplayName("A curse on the controller makes every creature they control a forced attacker")
    void aCurseOnTheControllerCountsForTheirCreatures() {
        // Curse of the Nightly Hunt enchants a player, not a creature.
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new CurseOfTheNightlyHunt());

        assertThat(als.getMustAttackRequirementCount(gd, bears)).isZero();

        curse.setAttachedTo(player1.getId());
        assertThat(als.getMustAttackRequirementCount(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("The defending player is always a legal attack target")
    void defendingPlayerIsAValidAttackTarget() {
        assertThat(als.getValidAttackTargetIds(gd, player1.getId())).contains(player2.getId());
        assertThat(als.buildAvailableTargets(gd, player1.getId()))
                .anyMatch(target -> target.id().equals(player2.getId()) && target.isPlayer());
    }

    @Test
    @DisplayName("CR 508.1c: the defending player's planeswalkers are attack targets too")
    void defendingPlaneswalkersAreValidAttackTargets() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        // A permanent the attacking player controls is never one of their own attack targets.
        harness.addToBattlefield(player1, new ChandraNalaar());

        assertThat(als.getValidAttackTargetIds(gd, player1.getId()))
                .containsExactlyInAnyOrder(player2.getId(), chandra.getId());
        assertThat(als.buildAvailableTargets(gd, player1.getId()))
                .anyMatch(target -> target.id().equals(chandra.getId()) && !target.isPlayer());
    }
}
