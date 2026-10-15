package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.Errantry;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NetherShadow;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SirensCall.class, GrizzlyBears.class, WallOfAir.class, NetherShadow.class})
class SirensCallTest extends BaseCardTest {

    /** player1 (caster) holds Siren's Call with mana; it's player2's turn, before attackers. */
    private void primeCall() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new SirensCall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
    }

    private void runEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        gd.interaction.clearAwaitingInput();
        harness.clearPriorityPassed();
        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(StepTriggerService.class).handleEndStepTriggers(gd));
    }

    @Test
    @DisplayName("Forces the active player's creatures to attack this turn if able")
    void forcesActivePlayersCreaturesToAttack() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        primeCall();

        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);

        declareAttackers(player2, List.of(0));
        assertThat(bear.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("At end step, destroys non-Wall creatures that didn't attack; spares Walls, attackers, and newly-controlled creatures")
    void destroysNonAttackersAtEndStep() {
        Permanent lazy = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttackedThisTurn(true);
        Permanent wall = addCreatureReady(player2, new WallOfAir());
        // Came under control this turn (summoning sick) => didn't control it since the turn began.
        Permanent fresh = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        primeCall();
        harness.castAndResolveInstant(player1, 0);

        runEndStep();
        harness.passBothPriorities();

        // Only the ready non-Wall creature that didn't attack is destroyed.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(lazy)
                .contains(attacker, wall, fresh);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a tapped non-Wall creature that did not attack")
    void destroysTappedNonWallCreature() {
        Permanent tapped = addCreatureReady(player2, new GrizzlyBears());
        tapped.tap();
        primeCall();

        harness.castAndResolveInstant(player1, 0);

        runEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tapped);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves creatures on the battlefield until the delayed destruction resolves")
    void putsDelayedDestructionOnStack() {
        Permanent lazy = addCreatureReady(player2, new GrizzlyBears());
        primeCall();

        harness.castAndResolveInstant(player1, 0);

        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(lazy);
        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @DisplayName("Requires a hasty creature entering later in the turn to attack if able")
    void requiresLaterCreatureToAttackIfAble() {
        primeCall();

        harness.castAndResolveInstant(player1, 0);
        harness.addToBattlefieldAndReturn(player2, new NetherShadow());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast during your own turn")
    void cannotCastOnYourOwnTurn() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SirensCall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot be cast once attackers are declared")
    void cannotCastAfterAttackersDeclared() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new SirensCall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot be cast before attackers in a later combat phase")
    void cannotCastDuringAdditionalCombat() {
        primeCall();
        gd.combatPhasesThisTurn = 2;

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The spell's controller controls its delayed destruction ability")
    void casterControlsDelayedDestruction() {
        Permanent lazy = addCreatureReady(player2, new GrizzlyBears());
        primeCall();
        harness.castAndResolveInstant(player1, 0);

        runEndStep();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(lazy);
    }

    @Test
    @DisplayName("A creature with haste entering after resolution is spared even if it did not attack")
    void sparesNewHastyNonAttacker() {
        primeCall();
        harness.castAndResolveInstant(player1, 0);
        Permanent shadow = harness.addToBattlefieldAndReturn(player2, new NetherShadow());
        shadow.tap();

        runEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shadow);
        harness.assertNotInGraveyard(player2, "Nether Shadow");
    }

    @Test
    @DisplayName("Does not destroy the caster's creatures that did not attack")
    void sparesOtherPlayersCreatures() {
        Permanent casterCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent activeCreature = addCreatureReady(player2, new GrizzlyBears());
        activeCreature.tap();
        primeCall();
        harness.castAndResolveInstant(player1, 0);

        runEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(casterCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(activeCreature);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Allows no attackers when only a defender and a summoning-sick creature are present")
    void doesNotRequireUnableCreaturesToAttack() {
        addCreatureReady(player2, new WallOfAir());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        primeCall();
        harness.castAndResolveInstant(player1, 0);

        declareAttackers(player2, List.of());

        harness.assertOnBattlefield(player2, "Wall of Air");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed(Errantry.class)
    @DisplayName("When the original creature can only attack alone, a later hasty creature may attack instead")
    void canChooseLaterHastyCreatureUnderAttackLimit() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Errantry());
        aura.setAttachedTo(bear.getId());
        primeCall();
        harness.castAndResolveInstant(player1, 0);
        Permanent shadow = harness.addToBattlefieldAndReturn(player2, new NetherShadow());

        declareAttackers(player2, List.of(1));

        assertThat(shadow.isAttackedThisTurn()).isTrue();
        assertThat(bear.isAttackedThisTurn()).isFalse();
    }
}
