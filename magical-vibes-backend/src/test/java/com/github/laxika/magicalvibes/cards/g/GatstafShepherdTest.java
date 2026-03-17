package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatstafShepherdTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        GatstafShepherd card = new GatstafShepherd();

        // No activated abilities
        assertThat(card.getActivatedAbilities()).isEmpty();

        // Each-upkeep transform trigger
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect conditional =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        // Back face exists
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("GatstafHowler");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        GatstafShepherd card = new GatstafShepherd();
        GatstafHowler backFace = (GatstafHowler) card.getBackFaceCard();

        // No activated abilities
        assertThat(backFace.getActivatedAbilities()).isEmpty();

        // Each-upkeep transform trigger
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    // ===== Werewolf transform: front → back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Gatstaf Howler when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new GatstafShepherd());
        Permanent shepherd = findPermanent(player1, "Gatstaf Shepherd");

        // spellsCastLastTurn is empty (no spells cast)
        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(shepherd.isTransformed()).isTrue();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");
        assertThat(gqs.getEffectivePower(gd, shepherd)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shepherd)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new GatstafShepherd());
        Permanent shepherd = findPermanent(player1, "Gatstaf Shepherd");

        // Simulate that a spell was cast last turn
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(shepherd.isTransformed()).isFalse();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Shepherd");
    }

    // ===== Werewolf transform: back → front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Gatstaf Howler transforms back when a player cast two or more spells last turn")
    void howlerTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new GatstafShepherd());
        Permanent shepherd = findPermanent(player1, "Gatstaf Shepherd");

        // Transform to Gatstaf Howler first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep
        harness.passBothPriorities(); // resolve transform
        assertThat(shepherd.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(shepherd.isTransformed()).isFalse();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Shepherd");
        assertThat(gqs.getEffectivePower(gd, shepherd)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shepherd)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gatstaf Howler does not transform back when only one spell was cast last turn")
    void howlerDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new GatstafShepherd());
        Permanent shepherd = findPermanent(player1, "Gatstaf Shepherd");

        // Transform to Gatstaf Howler first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(shepherd.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(shepherd.isTransformed()).isTrue();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new GatstafShepherd());
        Permanent shepherd = findPermanent(player1, "Gatstaf Shepherd");

        // No spells cast last turn
        gd.spellsCastLastTurn.clear();

        // Trigger on opponent's upkeep (not player1's)
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(shepherd.isTransformed()).isTrue();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
