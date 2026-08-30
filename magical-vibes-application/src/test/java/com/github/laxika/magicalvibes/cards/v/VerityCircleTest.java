package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerityCircleTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an opponent's creature lets the controller draw")
    void opponentCreatureTapDraws() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        tap(creature);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Tapping an opponent's creature while declaring it as an attacker does not trigger")
    void attackerDeclarationDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerityCircle());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping an attacking vigilance creature does trigger")
    void tappingAttackingVigilanceCreatureTriggers() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent attacker = addCreatureReady(player2, new SerraAngel());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0));
        tap(attacker);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Tapping your own creature does not trigger")
    void ownCreatureTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(creature);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability taps a target creature without flying")
    void abilityTapsNonflyingCreature() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a creature with flying")
    void abilityCannotTargetFlyingCreature() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = addCreatureReady(player2, new SerraAngel());
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature")
    void abilityCannotTargetNoncreature() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
