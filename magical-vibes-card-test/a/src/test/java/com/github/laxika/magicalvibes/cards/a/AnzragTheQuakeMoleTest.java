package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GravestoneStrider;
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

@CardUsed({AnzragTheQuakeMole.class, GravestoneStrider.class})
class AnzragTheQuakeMoleTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked untaps each creature you control and grants an additional combat")
    void becomingBlockedUntapsCreaturesAndGrantsAdditionalCombat() {
        Permanent anzrag = addCreatureReady(player1, new AnzragTheQuakeMole());
        Permanent attacker = addCreatureReady(player1, new GravestoneStrider());
        Permanent tappedCreature = addCreatureReady(player1, new GravestoneStrider());
        tappedCreature.tap();
        addCreatureReady(player2, new GravestoneStrider());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(anzrag.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability makes Anzrag must be blocked this turn")
    void activatedAbilityMakesSourceMustBeBlocked() {
        harness.addToBattlefield(player1, new AnzragTheQuakeMole());
        addCreatureReady(player2, new GravestoneStrider());
        addCreatureReady(player2, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent anzrag = findPermanent(player1, "Anzrag, the Quake-Mole");
        assertThat(anzrag.isMustBeBlockedThisTurn()).isTrue();

        anzrag.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");
    }

    @Test
    @DisplayName("Activated must-be-blocked requirement wears off at end of turn")
    void activatedRequirementWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AnzragTheQuakeMole());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent anzrag = findPermanent(player1, "Anzrag, the Quake-Mole");
        assertThat(anzrag.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Assigning the only blocker elsewhere cannot evade Anzrag's requirement")
    void cannotEvadeRequirementByBlockingAnotherAttacker() {
        addCreatureReady(player1, new AnzragTheQuakeMole());
        addCreatureReady(player1, new GravestoneStrider());
        addCreatureReady(player2, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");
    }

    @Test
    @DisplayName("Multiple blockers cause only one becomes-blocked trigger")
    void multipleBlockersTriggerOnlyOnce() {
        Permanent anzrag = addCreatureReady(player1, new AnzragTheQuakeMole());
        addCreatureReady(player2, new GravestoneStrider());
        addCreatureReady(player2, new GravestoneStrider());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(anzrag.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Only one creature must block Anzrag; another may remain unassigned")
    void oneBlockerSatisfiesRequirement() {
        addCreatureReady(player1, new AnzragTheQuakeMole());
        addCreatureReady(player2, new GravestoneStrider());
        addCreatureReady(player2, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped opposing creatures need not block and are not untapped")
    void tappedOpposingCreatureCannotBlock() {
        addCreatureReady(player1, new AnzragTheQuakeMole());
        Permanent defender = addCreatureReady(player2, new GravestoneStrider());
        defender.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent anzrag = findPermanent(player1, "Anzrag, the Quake-Mole");
        anzrag.setAttacking(true);
        anzrag.tap();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(anzrag.isTapped()).isTrue();
        assertThat(defender.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }
}

