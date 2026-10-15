package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GraniticTitan;
import com.github.laxika.magicalvibes.cards.h.HarrierNaga;
import com.github.laxika.magicalvibes.cards.r.RampagingHippo;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sandblast.class, HarrierNaga.class, GraniticTitan.class, RampagingHippo.class})
class SandblastTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sandblast targeting an attacking creature puts it on the stack")
    void castingTargetingAttackingCreature() {
        Permanent attacker = addCreatureReady(player1, new HarrierNaga());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sandblast()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent attacker = addCreatureReady(player1, new GraniticTitan());
        attacker.setAttacking(true);

        harness.addToBattlefield(player1, new HarrierNaga());
        UUID targetId = harness.getPermanentId(player1, "Harrier Naga");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sandblast()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent attacker = addCreatureReady(player1, new HarrierNaga());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sandblast()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell cannot target players");
    }

    @Test
    @DisplayName("Resolving deals 5 damage to a blocking creature, killing it")
    void resolvingDeals5DamageToBlockingCreature() {
        Permanent blocker = addCreatureReady(player2, new GraniticTitan());
        blocker.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sandblast()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, blocker.getId());

        // Granitic Titan has 4 toughness, so 5 damage kills it
        harness.assertNotOnBattlefield(player2, "Granitic Titan");
        harness.assertInGraveyard(player2, "Granitic Titan");
    }

    @Test
    @DisplayName("Sandblast fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent attacker = addCreatureReady(player1, new HarrierNaga());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sandblast()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Sandblast deals exactly five damage to an attacking creature")
    void dealsExactlyFiveDamageToAttacker() {
        Permanent attacker = addCreatureReady(player1, new RampagingHippo());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sandblast()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        harness.assertOnBattlefield(player1, "Rampaging Hippo");
        assertThat(attacker.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player2, "Sandblast");
    }

    @Test
    @DisplayName("Sandblast does not damage a creature that stops attacking before resolution")
    void targetStopsAttackingBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new HarrierNaga());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sandblast()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Harrier Naga");
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player2, "Sandblast");
    }

    @Test
    @DisplayName("Sandblast does not damage a creature that stops blocking before resolution")
    void targetStopsBlockingBeforeResolution() {
        Permanent blocker = addCreatureReady(player2, new HarrierNaga());
        blocker.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sandblast()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, blocker.getId());

        blocker.setBlocking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Harrier Naga");
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player2, "Sandblast");
    }
}
