package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KarplusanHoundTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ATTACK trigger with ControlsSubtypeConditionalEffect(CHANDRA) wrapping DealDamageToAnyTargetEffect(2)")
    void hasCorrectStructure() {
        KarplusanHound card = new KarplusanHound();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(ControlsSubtypeConditionalEffect.class);
        ControlsSubtypeConditionalEffect conditional =
                (ControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.CHANDRA);
        assertThat(conditional.wrapped()).isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect damage = (DealDamageToAnyTargetEffect) conditional.wrapped();
        assertThat(damage.damage()).isEqualTo(2);
    }

    // ===== Trigger fires when controlling Chandra =====

    @Test
    @DisplayName("Attacking with a Chandra planeswalker on the battlefield queues target selection")
    void attackTriggerFiresWhenControllingChandra() {
        addCreatureReady(player1, new KarplusanHound());
        addPlaneswalker(player1, new ChandraBoldPyromancer());
        addCreatureReady(player2, createVanillaCreature());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Deals 2 damage to chosen creature when Chandra is controlled")
    void deals2DamageToTargetCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new KarplusanHound());
        addPlaneswalker(player1, new ChandraBoldPyromancer());
        Permanent opponentCreature = addCreatureReady(player2, createVanillaCreature());

        declareAttackers(player1, List.of(0));

        // Choose the opponent's creature as target
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Opponent's creature (2/2) should have taken 2 damage and died
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("Deals 2 damage to target player when Chandra is controlled")
    void deals2DamageToTargetPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new KarplusanHound());
        addPlaneswalker(player1, new ChandraBoldPyromancer());

        declareAttackers(player1, List.of(0));

        // Choose the opponent player as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Opponent should have taken 2 trigger damage + 3 combat damage from the 3/3 Hound = 5 total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    // ===== Trigger does NOT fire without Chandra =====

    @Test
    @DisplayName("Attacking without a Chandra planeswalker does NOT trigger the ability")
    void noTriggerWithoutChandra() {
        addCreatureReady(player1, new KarplusanHound());

        declareAttackers(player1, List.of(0));

        // No trigger should be on the stack
        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Karplusan Hound"));
        // No pending target selection
        assertThat(gd.pendingAttackTriggerTargets).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addPlaneswalker(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createVanillaCreature() {
        // GrizzlyBears is a 2/2 vanilla creature
        return new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
