package com.github.laxika.magicalvibes.service.combat.block;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LightOfDay;
import com.github.laxika.magicalvibes.cards.m.MaraudingBoneslasher;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.t.TrainingDrone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec for {@link BlockLegalityService#canBlock}, the creature-level half of declare-blockers
 * legality (CR 509.1a): may this permanent be declared as a blocker at all, independent of which
 * attacker it would block. The attacker-dependent half lives in {@link BlockLegalityContextTest}.
 *
 * <p>Each test pins one clause of the gate, so a clause that silently stops being consulted fails
 * here rather than only surfacing as a wrong list of offered blockers deep inside
 * {@code CombatBlockService.getBlockableCreatureIndices} or the AI's blocker search.
 */
class BlockLegalityServiceTest extends BaseCardTest {

    @Test
    @DisplayName("An untapped creature can block")
    void untappedCreatureCanBlock() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlock(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("A tapped creature can't block")
    void tappedCreatureCannotBlock() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        assertThat(bls.canBlock(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("CR 302.6: summoning sickness does not stop a creature from blocking")
    void summoningSickCreatureCanBlock() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isSummoningSick()).isTrue();

        assertThat(bls.canBlock(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("A non-creature permanent can't block")
    void nonCreaturePermanentCannotBlock() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");

        assertThat(bls.canBlock(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("The \"can't block this turn\" flag stops blocking")
    void cantBlockThisTurnFlagStopsBlocking() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCantBlockThisTurn(true);

        assertThat(bls.canBlock(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("A printed \"can't block\" static stops blocking")
    void staticCantBlockStopsBlocking() {
        Permanent glider = addCreatureReady(player2, new AesthirGlider());

        assertThat(bls.canBlock(gd, glider)).isFalse();
    }

    @Test
    @DisplayName("An aura granting \"can't attack or block\" stops the enchanted creature blocking")
    void auraCantAttackOrBlockStopsBlocking() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player2, new Pacifism());

        assertThat(bls.canBlock(gd, bears)).isTrue();

        pacifism.setAttachedTo(bears.getId());
        assertThat(bls.canBlock(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("\"Can't attack or block unless equipped\" is gated on an attached Equipment")
    void equipmentRequirementGatesBlocking() {
        Permanent drone = addCreatureReady(player2, new TrainingDrone());
        Permanent scimitar = addCreatureReady(player2, new LeoninScimitar());

        // On the battlefield but attached to nothing — the requirement is not met.
        assertThat(bls.canBlock(gd, drone)).isFalse();

        scimitar.setAttachedTo(drone.getId());
        assertThat(bls.canBlock(gd, drone)).isTrue();

        scimitar.setAttachedTo(null);
        assertThat(bls.canBlock(gd, drone)).isFalse();
    }

    @Test
    @DisplayName("\"Can't block unless …\" is re-evaluated against the current board")
    void cantBlockUnlessConditionGatesBlocking() {
        // Marauding Boneslasher: "can't block unless you control another Zombie". It is itself a
        // Zombie, so the condition turns on the *other* Zombie and not on its own subtypes.
        Permanent boneslasher = addCreatureReady(player2, new MaraudingBoneslasher());

        assertThat(bls.canBlock(gd, boneslasher)).isFalse();

        // A Zombie the opponent controls is not one "you control".
        addCreatureReady(player1, new ScatheZombies());
        assertThat(bls.canBlock(gd, boneslasher)).isFalse();

        addCreatureReady(player2, new ScatheZombies());
        assertThat(bls.canBlock(gd, boneslasher)).isTrue();
    }

    @Test
    @DisplayName("A board-wide \"can't attack or block\" restriction applies to matching creatures only")
    void globalCantAttackOrBlockRestrictionStopsBlocking() {
        // Light of Day: "Black creatures can't attack or block."
        Permanent zombies = addCreatureReady(player2, new ScatheZombies());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlock(gd, zombies)).isTrue();

        harness.addToBattlefield(player1, new LightOfDay());

        assertThat(bls.canBlock(gd, zombies)).isFalse();
        assertThat(bls.canBlock(gd, bears)).isTrue();
    }
}
