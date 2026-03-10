package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantAttackIfCastSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AngelicArbiterTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Angelic Arbiter has correct static effects")
    void hasCorrectEffects() {
        AngelicArbiter card = new AngelicArbiter();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .hasSize(2)
                .anySatisfy(e -> assertThat(e).isInstanceOf(OpponentsCantAttackIfCastSpellThisTurnEffect.class))
                .anySatisfy(e -> assertThat(e).isInstanceOf(OpponentsCantCastSpellsIfAttackedThisTurnEffect.class));
    }

    // ===== Can't attack if cast a spell =====

    @Test
    @DisplayName("Opponent who cast a spell this turn cannot attack")
    void cantAttackIfCastSpell() {
        // Player2 controls Angelic Arbiter
        harness.addToBattlefield(player2, new AngelicArbiter());

        // Player1 has a creature to attack with
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        // Player1 casts a spell this turn
        gd.spellsCastThisTurn.put(player1.getId(), 1);

        // Try to attack
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

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
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        // Player1 has NOT cast a spell this turn

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Should succeed — no spell was cast. The call not throwing proves the creature can attack.
        gs.declareAttackers(gd, player1, List.of(0));
    }

    // ===== Can't cast spells if attacked =====

    @Test
    @DisplayName("Opponent who attacked this turn cannot cast spells")
    void cantCastIfAttacked() {
        // Player2 controls Angelic Arbiter
        harness.addToBattlefield(player2, new AngelicArbiter());

        // Player1 has attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        // Player1 has a spell in hand
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        GameBroadcastService gbs = harness.getGameBroadcastService();
        List<Integer> playable = gbs.getPlayableCardIndices(gd, player1.getId());

        // Shock should NOT be playable because player attacked this turn
        assertThat(playable).isEmpty();
    }

    @Test
    @DisplayName("Opponent who did not attack can still cast spells")
    void canCastIfDidNotAttack() {
        // Player2 controls Angelic Arbiter
        harness.addToBattlefield(player2, new AngelicArbiter());

        // Player1 has NOT attacked this turn

        // Player1 has a spell in hand
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        GameBroadcastService gbs = harness.getGameBroadcastService();
        List<Integer> playable = gbs.getPlayableCardIndices(gd, player1.getId());

        // Shock should be playable — no attack was declared
        assertThat(playable).contains(0);
    }

    // ===== Controller is not restricted =====

    @Test
    @DisplayName("Controller of Angelic Arbiter can attack after casting a spell")
    void controllerCanAttackAfterCasting() {
        // Player1 controls Angelic Arbiter
        harness.addToBattlefield(player1, new AngelicArbiter());

        // Player1 has a creature to attack with
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        // Player1 cast a spell this turn
        gd.spellsCastThisTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Angelic Arbiter is at index 0 (summoning sick), GrizzlyBears is at index 1
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

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        GameBroadcastService gbs = harness.getGameBroadcastService();
        List<Integer> playable = gbs.getPlayableCardIndices(gd, player1.getId());

        // Shock should be playable — the restriction only applies to opponents
        assertThat(playable).contains(0);
    }

    // ===== Land plays are not affected =====

    @Test
    @DisplayName("Opponent who attacked can still play lands")
    void canPlayLandAfterAttacking() {
        // Player2 controls Angelic Arbiter
        harness.addToBattlefield(player2, new AngelicArbiter());

        // Player1 attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        // Player1 has a land in hand
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.f.Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        GameBroadcastService gbs = harness.getGameBroadcastService();
        List<Integer> playable = gbs.getPlayableCardIndices(gd, player1.getId());

        // Land should still be playable
        assertThat(playable).contains(0);
    }
}
