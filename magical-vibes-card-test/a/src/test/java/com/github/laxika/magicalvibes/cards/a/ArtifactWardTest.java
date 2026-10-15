package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.Caltrops;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.SoldeviGolem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtifactWard.class, BalduvianBears.class, GrizzlyBears.class, IcyManipulator.class,
        ProdigalSorcerer.class, SoldeviGolem.class})
class ArtifactWardTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't be blocked by artifact creatures")
    void cannotBeBlockedByArtifactCreature() {
        Permanent attacker = addWardedCreature(player1);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SoldeviGolem());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by nonartifact creatures")
    void canBeBlockedByNonartifactCreature() {
        Permanent attacker = addWardedCreature(player1);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can't be targeted by an artifact ability")
    void artifactAbilityCannotTargetEnchantedCreature() {
        Permanent creature = addWardedCreature(player1);
        Permanent manipulator = addCreatureReady(player1, new IcyManipulator());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manipulator), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact sources");
        assertThat(manipulator.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can be targeted by a nonartifact ability")
    void nonartifactAbilityCanTargetEnchantedCreature() {
        Permanent creature = addWardedCreature(player2);
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sorcerer), null,
                creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents combat damage from artifact sources")
    void preventsArtifactCombatDamage() {
        Permanent creature = addWardedCreature(player2);
        Permanent attacker = addCreatureReady(player1, new SoldeviGolem());
        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(creature),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage from nonartifact sources")
    void allowsNonartifactCombatDamage() {
        Permanent creature = addWardedCreature(player2);
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(creature),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed({Caltrops.class})
    @DisplayName("Prevents nontargeted noncombat damage from an artifact")
    void preventsNontargetedArtifactDamage() {
        Permanent attacker = addWardedCreature(player1);
        harness.addToBattlefield(player2, new Caltrops());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1,
                        List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Artifact ability resolves when the Ward is attached to another creature")
    void artifactAbilityCanTargetAnotherCreature() {
        addWardedCreature(player2);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent manipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manipulator), null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An artifact ability loses its target when Artifact Ward is attached before resolution")
    void pendingArtifactAbilityLosesTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent manipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manipulator), null, creature.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ArtifactWard());
        aura.setAttachedTo(creature.getId());

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Artifact targeting restriction ends when the Aura leaves")
    void artifactTargetingRestrictionEndsWithAura() {
        Permanent creature = addWardedCreature(player2);
        gd.playerBattlefields.get(player2.getId()).removeIf(
                permanent -> permanent.getCard() instanceof ArtifactWard);
        Permanent manipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manipulator), null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent addWardedCreature(Player owner) {
        Permanent creature = addCreatureReady(owner, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(owner, new ArtifactWard());
        aura.setAttachedTo(creature.getId());
        return creature;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }

}
