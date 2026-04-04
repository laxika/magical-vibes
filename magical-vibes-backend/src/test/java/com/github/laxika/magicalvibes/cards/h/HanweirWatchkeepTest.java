package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BaneOfHanweir;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HanweirWatchkeepTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        HanweirWatchkeep card = new HanweirWatchkeep();

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
        assertThat(card.getBackFaceClassName()).isEqualTo("BaneOfHanweir");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        HanweirWatchkeep card = new HanweirWatchkeep();
        BaneOfHanweir backFace = (BaneOfHanweir) card.getBackFaceCard();

        // No activated abilities
        assertThat(backFace.getActivatedAbilities()).isEmpty();

        // Must attack static effect
        assertThat(backFace.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(MustAttackEffect.class);

        // Each-upkeep transform trigger
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    // ===== Werewolf transform: front → back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Bane of Hanweir when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new HanweirWatchkeep());
        Permanent watchkeep = findPermanent(player1, "Hanweir Watchkeep");

        // spellsCastLastTurn is empty (no spells cast)
        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(watchkeep.isTransformed()).isTrue();
        assertThat(watchkeep.getCard().getName()).isEqualTo("Bane of Hanweir");
        assertThat(gqs.getEffectivePower(gd, watchkeep)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, watchkeep)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new HanweirWatchkeep());
        Permanent watchkeep = findPermanent(player1, "Hanweir Watchkeep");

        // Simulate that a spell was cast last turn
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(watchkeep.isTransformed()).isFalse();
        assertThat(watchkeep.getCard().getName()).isEqualTo("Hanweir Watchkeep");
    }

    // ===== Werewolf transform: back → front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Bane of Hanweir transforms back when a player cast two or more spells last turn")
    void baneTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new HanweirWatchkeep());
        Permanent watchkeep = findPermanent(player1, "Hanweir Watchkeep");

        // Transform to Bane of Hanweir first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep
        harness.passBothPriorities(); // resolve transform
        assertThat(watchkeep.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(watchkeep.isTransformed()).isFalse();
        assertThat(watchkeep.getCard().getName()).isEqualTo("Hanweir Watchkeep");
        assertThat(gqs.getEffectivePower(gd, watchkeep)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, watchkeep)).isEqualTo(5);
    }

    @Test
    @DisplayName("Bane of Hanweir does not transform back when only one spell was cast last turn")
    void baneDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new HanweirWatchkeep());
        Permanent watchkeep = findPermanent(player1, "Hanweir Watchkeep");

        // Transform to Bane of Hanweir first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(watchkeep.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(watchkeep.isTransformed()).isTrue();
        assertThat(watchkeep.getCard().getName()).isEqualTo("Bane of Hanweir");
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new HanweirWatchkeep());
        Permanent watchkeep = findPermanent(player1, "Hanweir Watchkeep");

        // No spells cast last turn
        gd.spellsCastLastTurn.clear();

        // Trigger on opponent's upkeep (not player1's)
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(watchkeep.isTransformed()).isTrue();
        assertThat(watchkeep.getCard().getName()).isEqualTo("Bane of Hanweir");
    }

    // ===== Helpers =====

}
