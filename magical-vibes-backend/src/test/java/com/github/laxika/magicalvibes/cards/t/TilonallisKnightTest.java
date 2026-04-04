package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TilonallisKnightTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ATTACK trigger with ControlsSubtypeConditionalEffect(DINOSAUR) wrapping BoostSelfEffect(1, 1)")
    void hasCorrectStructure() {
        TilonallisKnight card = new TilonallisKnight();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(ControlsSubtypeConditionalEffect.class);
        ControlsSubtypeConditionalEffect conditional =
                (ControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.DINOSAUR);
        assertThat(conditional.wrapped()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect boost = (BoostSelfEffect) conditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    // ===== Trigger fires when controlling a Dinosaur =====

    @Test
    @DisplayName("Gets +1/+1 when attacking while controlling a Dinosaur")
    void boostsOnAttackWithDinosaur() {
        Permanent knight = addCreatureReady(player1, new TilonallisKnight());
        addCreatureReady(player1, createDinosaur());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isEqualTo(1);
        assertThat(knight.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1/+1 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent knight = addCreatureReady(player1, new TilonallisKnight());
        addCreatureReady(player1, createDinosaur());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getPowerModifier()).isEqualTo(0);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Trigger does NOT fire without a Dinosaur =====

    @Test
    @DisplayName("Attacking without a Dinosaur does NOT trigger the ability")
    void noTriggerWithoutDinosaur() {
        addCreatureReady(player1, new TilonallisKnight());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Tilonalli's Knight"));
    }

    @Test
    @DisplayName("No boost when attacking without a Dinosaur")
    void noBoostWithoutDinosaur() {
        Permanent knight = addCreatureReady(player1, new TilonallisKnight());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isEqualTo(0);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Opponent's Dinosaur doesn't count =====

    @Test
    @DisplayName("Opponent's Dinosaur does not trigger the ability")
    void opponentDinosaurDoesNotCount() {
        addCreatureReady(player1, new TilonallisKnight());
        addCreatureReady(player2, createDinosaur());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Tilonalli's Knight"));
    }

    // ===== Helper methods =====


    private Card createDinosaur() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.DINOSAUR));
        return card;
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
