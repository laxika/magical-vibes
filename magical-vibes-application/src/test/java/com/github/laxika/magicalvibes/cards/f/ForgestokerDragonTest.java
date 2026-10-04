package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForgestokerDragon.class, GrizzlyBears.class, LlanowarElves.class})
class ForgestokerDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target creature while attacking")
    void dealsDamageWhileAttacking() {
        Permanent dragon = addCreatureReady(player1, new ForgestokerDragon());
        addCreatureReady(player2, new LlanowarElves());
        setUpAttacking(dragon);

        Permanent target = findPermanent(player2, "Llanowar Elves");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Target creature cannot block this combat after resolution")
    void targetCannotBlockThisCombat() {
        Permanent dragon = addCreatureReady(player1, new ForgestokerDragon());
        Permanent groundAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        setUpAttacking(dragon);
        groundAttacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        findPermanent(player1, "Forgestoker Dragon").setAttacking(true);
        findPermanent(player1, "Grizzly Bears").setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while not attacking")
    void cannotActivateWhenNotAttacking() {
        addCreatureReady(player1, new ForgestokerDragon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking");
    }

    @Test
    void canActivateMultipleTimesWhileTappedAndAttacking() {
        Permanent dragon = addCreatureReady(player1, new ForgestokerDragon());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        setUpAttacking(dragon);
        dragon.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canTargetItself() {
        Permanent dragon = addCreatureReady(player1, new ForgestokerDragon());
        setUpAttacking(dragon);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, dragon.getId());
        harness.passBothPriorities();

        assertThat(dragon.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Forgestoker Dragon");
    }

    @Test
    void resolvesEvenIfDragonIsNoLongerAttacking() {
        Permanent dragon = addCreatureReady(player1, new ForgestokerDragon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setUpAttacking(dragon);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        dragon.setAttacking(false);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisCombat()).isTrue();
    }

    @Test
    void resolvesAfterDragonDies() {
        Permanent dragon = addCreatureReady(player1, new ForgestokerDragon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setUpAttacking(dragon);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        dragon.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Forgestoker Dragon");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisCombat()).isTrue();
    }

    @Test
    void doesNotUndoAnExistingBlock() {
        addCreatureReady(player1, new ForgestokerDragon());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))));

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(attacker.getId());
        harness.resolveCombatDamage();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void blockingRestrictionExpiresWhenCombatEnds() {
        Permanent dragon = addCreatureReady(player1, new ForgestokerDragon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setUpAttacking(dragon);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisCombat()).isTrue();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(target.isCantBlockThisCombat()).isFalse();
    }

    private void setUpAttacking(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
    }
}
