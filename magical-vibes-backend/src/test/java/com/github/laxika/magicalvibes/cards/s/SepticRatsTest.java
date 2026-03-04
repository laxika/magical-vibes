package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerPoisonedConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SepticRatsTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ATTACK trigger with DefendingPlayerPoisonedConditionalEffect wrapping BoostSelfEffect(1,1)")
    void hasCorrectStructure() {
        SepticRats card = new SepticRats();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(DefendingPlayerPoisonedConditionalEffect.class);
        DefendingPlayerPoisonedConditionalEffect conditional =
                (DefendingPlayerPoisonedConditionalEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect boost = (BoostSelfEffect) conditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    // ===== Attack trigger fires =====

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        Permanent rats = addCreatureReady(player1, new SepticRats());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Septic Rats"));
    }

    // ===== Conditional boost — defending player poisoned =====

    @Test
    @DisplayName("Gets +1/+1 when attacking if defending player is poisoned")
    void boostsWhenDefendingPlayerPoisoned() {
        Permanent rats = addCreatureReady(player1, new SepticRats());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(rats.getPowerModifier()).isEqualTo(1);
        assertThat(rats.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does NOT get +1/+1 when attacking if defending player has no poison counters")
    void noBoostWhenDefendingPlayerNotPoisoned() {
        Permanent rats = addCreatureReady(player1, new SepticRats());
        // No poison counters on defender

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(rats.getPowerModifier()).isEqualTo(0);
        assertThat(rats.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Checks defending player (opponent), not controller for poison")
    void checksDefendingPlayerNotController() {
        Permanent rats = addCreatureReady(player1, new SepticRats());
        // Controller has poison but defender does not
        gd.playerPoisonCounters.put(player1.getId(), 3);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(rats.getPowerModifier()).isEqualTo(0);
        assertThat(rats.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
