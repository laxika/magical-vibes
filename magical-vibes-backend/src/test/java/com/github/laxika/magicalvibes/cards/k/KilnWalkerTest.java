package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KilnWalkerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ATTACK trigger with BoostSelfEffect(3, 0)")
    void hasCorrectStructure() {
        KilnWalker card = new KilnWalker();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(3);
        assertThat(effect.toughnessBoost()).isEqualTo(0);
    }

    // ===== Attack trigger fires =====

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new KilnWalker());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Kiln Walker"));
    }

    @Test
    @DisplayName("Gets +3/+0 when attacking and trigger resolves")
    void boostsOnAttack() {
        Permanent walker = addCreatureReady(player1, new KilnWalker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(walker.getPowerModifier()).isEqualTo(3);
        assertThat(walker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("+3/+0 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent walker = addCreatureReady(player1, new KilnWalker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(walker.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(walker.getPowerModifier()).isEqualTo(0);
        assertThat(walker.getToughnessModifier()).isEqualTo(0);
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
