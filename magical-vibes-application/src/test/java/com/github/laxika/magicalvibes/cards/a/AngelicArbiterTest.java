package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Snakeform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicArbiter.class, RuneclawBear.class, LightningBolt.class, Forest.class})
class AngelicArbiterTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent who cast a spell this turn cannot attack")
    void cantAttackIfCastSpell() {
        // Player2 controls Angelic Arbiter
        harness.addToBattlefield(player2, new AngelicArbiter());

        // Player1 has a creature to attack with
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        creature.setSummoningSick(false);

        // Player1 casts a spell this turn
        gd.recordSpellCast(player1.getId(), new RuneclawBear());

        // Try to attack
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Should throw because player cast a spell and opponent has Angelic Arbiter
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Opponent who did not cast a spell can still attack")
    void canAttackIfNoSpellCast() {
        // Player2 controls Angelic Arbiter
        harness.addToBattlefield(player2, new AngelicArbiter());

        // Player1 has a creature to attack with
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        creature.setSummoningSick(false);

        // Player1 has NOT cast a spell this turn

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Should succeed — no spell was cast. The call not throwing proves the creature can attack.
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Opponent who attacked this turn cannot cast spells")
    void cantCastIfAttacked() {
        // Player2 controls Angelic Arbiter
        harness.addToBattlefield(player2, new AngelicArbiter());

        // Player1 has attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        // Player1 has a spell in hand
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        List<Integer> playable = gbs.getPlayableCardIndices(gd, player1.getId());

        // LightningBolt should NOT be playable because player attacked this turn
        assertThat(playable).isEmpty();
    }

    @Test
    @DisplayName("Opponent who did not attack can still cast spells")
    void canCastIfDidNotAttack() {
        // Player2 controls Angelic Arbiter
        harness.addToBattlefield(player2, new AngelicArbiter());

        // Player1 has NOT attacked this turn

        // Player1 has a spell in hand
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        List<Integer> playable = gbs.getPlayableCardIndices(gd, player1.getId());

        // LightningBolt should be playable — no attack was declared
        assertThat(playable).contains(0);
    }

    @Test
    @DisplayName("Controller of Angelic Arbiter can attack after casting a spell")
    void controllerCanAttackAfterCasting() {
        // Player1 controls Angelic Arbiter
        harness.addToBattlefield(player1, new AngelicArbiter());

        // Player1 has a creature to attack with
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        creature.setSummoningSick(false);

        // Player1 cast a spell this turn
        gd.recordSpellCast(player1.getId(), new RuneclawBear());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Angelic Arbiter is at index 0 (summoning sick), RuneclawBear is at index 1
        // The call succeeding proves the controller is not restricted by their own Arbiter
        gs.declareAttackers(gd, player1, List.of(1));
    }

    @Test
    @DisplayName("Controller of Angelic Arbiter can cast spells after attacking")
    void controllerCanCastAfterAttacking() {
        // Player1 controls Angelic Arbiter
        harness.addToBattlefield(player1, new AngelicArbiter());

        // Player1 attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        List<Integer> playable = gbs.getPlayableCardIndices(gd, player1.getId());

        // LightningBolt should be playable — the restriction only applies to opponents
        assertThat(playable).contains(0);
    }

    @Test
    @CardUsed({Snakeform.class})
    @DisplayName("Arbiter that lost all abilities does not prevent attacks after casting")
    void canAttackAfterArbiterLosesAbilities() {
        Permanent arbiter = harness.addToBattlefieldAndReturn(player2, new AngelicArbiter());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        creature.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Snakeform()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, arbiter.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Casting restriction ends when Arbiter leaves the battlefield")
    void canCastAfterArbiterLeaves() {
        harness.addToBattlefield(player2, new AngelicArbiter());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).isEmpty();

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Arbiter entering later still counts an earlier attack")
    void earlierAttackPreventsCastingAfterArbiterEnters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        creature.setSummoningSick(false);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.addToBattlefield(player2, new AngelicArbiter());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attack restriction ends when Arbiter leaves the battlefield")
    void canAttackAfterArbiterLeaves() {
        harness.addToBattlefield(player2, new AngelicArbiter());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        creature.setSummoningSick(false);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Declaring no attackers does not prevent casting spells")
    void canCastAfterDeclaringNoAttackers() {
        harness.addToBattlefield(player2, new AngelicArbiter());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Opponent who attacked can still play lands")
    void canPlayLandAfterAttacking() {
        // Player2 controls Angelic Arbiter
        harness.addToBattlefield(player2, new AngelicArbiter());

        // Player1 attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        // Player1 has a land in hand
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        List<Integer> playable = gbs.getPlayableCardIndices(gd, player1.getId());

        // Land should still be playable
        assertThat(playable).contains(0);
    }
}
