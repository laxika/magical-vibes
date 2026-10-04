package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.v.VaporSnag;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmWithAether.class, GrizzlyBears.class, LlanowarElves.class, Forest.class,
        PincherBeetles.class, ProdigalPyromancer.class, VaporSnag.class})
class ArmWithAetherTest extends BaseCardTest {

    private void castAndResolveArmWithAether() {
        harness.setHand(player1, List.of(new ArmWithAether()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Casting Arm with Aether grants bounce ability to all controlled creatures")
    void grantsAbilityToControlledCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        castAndResolveArmWithAether();

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);
        elves.setAttacking(true);
        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Arm with Aether does not affect opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        castAndResolveArmWithAether();

        opponentBears.setAttacking(true);
        resolveCombat(player2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature with granted ability triggers bounce on combat damage to player")
    void triggersBounceOnCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castAndResolveArmWithAether();
        attacker.setAttacking(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).containsExactly(target.getId());
    }

    @Test
    @DisplayName("Selecting a creature to bounce returns it to owner's hand")
    void bouncesSelectedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castAndResolveArmWithAether();
        attacker.setAttacking(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The targeted bounce may be declined when the ability resolves")
    void mayDeclineBounce() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castAndResolveArmWithAether();
        attacker.setAttacking(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Bounce only targets creatures, not non-creature permanents")
    void onlyTargetsCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castAndResolveArmWithAether();
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new Forest());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("No trigger when attacker is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castAndResolveArmWithAether();
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Creature without granted ability does not trigger bounce")
    void noTriggerWithoutGrantedAbility() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        // NOT setting hasDamageToOpponentCreatureBounce
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Full flow: cast Arm with Aether, attack, bounce opponent creature")
    void fullFlow() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castAndResolveArmWithAether();

        // Now attack
        attacker.setAttacking(true);
        resolveCombat();

        // Should prompt for creature bounce
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the opponent's creature
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Noncombat damage to an opponent triggers the granted ability")
    void triggersOnNoncombatDamage() {
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castAndResolveArmWithAether();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(target.getId());
    }

    @Test
    @DisplayName("Shroud prevents the granted ability from targeting a creature")
    void cannotBounceCreatureWithShroud() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new PincherBeetles());
        castAndResolveArmWithAether();
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Pincher Beetles");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two casts grant two independent bounce abilities")
    void multipleCastsGrantIndependentAbilities() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new LlanowarElves());
        castAndResolveArmWithAether();
        castAndResolveArmWithAether();
        attacker.setAttacking(true);

        resolveCombat();

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain the ability")
    void laterCreatureDoesNotGainAbility() {
        castAndResolveArmWithAether();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The granted ability expires during cleanup")
    void abilityExpiresAtCleanup() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castAndResolveArmWithAether();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.setLife(player2, 20);
        attacker.setAttacking(true);

        resolveCombat(player1);

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The bounce cannot choose a replacement when its target leaves in response")
    void cannotRetargetWhenTargetLeaves() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new LlanowarElves());
        castAndResolveArmWithAether();
        attacker.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new VaporSnag()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
