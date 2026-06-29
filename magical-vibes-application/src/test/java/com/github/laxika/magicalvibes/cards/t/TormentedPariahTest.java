package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RampagingWerewolf;
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

class TormentedPariahTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        TormentedPariah card = new TormentedPariah();

        assertThat(card.getActivatedAbilities()).isEmpty();

        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect conditional =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("RampagingWerewolf");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        TormentedPariah card = new TormentedPariah();
        RampagingWerewolf backFace = (RampagingWerewolf) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).isEmpty();

        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    // ===== Werewolf transform: front -> back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Rampaging Werewolf when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new TormentedPariah());
        Permanent pariah = findPermanent(player1, "Tormented Pariah");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(pariah.isTransformed()).isTrue();
        assertThat(pariah.getCard().getName()).isEqualTo("Rampaging Werewolf");
        assertThat(gqs.getEffectivePower(gd, pariah)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, pariah)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new TormentedPariah());
        Permanent pariah = findPermanent(player1, "Tormented Pariah");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(pariah.isTransformed()).isFalse();
        assertThat(pariah.getCard().getName()).isEqualTo("Tormented Pariah");
    }

    // ===== Werewolf transform: back -> front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Rampaging Werewolf transforms back when a player cast two or more spells last turn")
    void werewolfTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new TormentedPariah());
        Permanent pariah = findPermanent(player1, "Tormented Pariah");

        // Transform to Rampaging Werewolf first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(pariah.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(pariah.isTransformed()).isFalse();
        assertThat(pariah.getCard().getName()).isEqualTo("Tormented Pariah");
        assertThat(gqs.getEffectivePower(gd, pariah)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pariah)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rampaging Werewolf does not transform back when only one spell was cast last turn")
    void werewolfDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new TormentedPariah());
        Permanent pariah = findPermanent(player1, "Tormented Pariah");

        // Transform to Rampaging Werewolf first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(pariah.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(pariah.isTransformed()).isTrue();
        assertThat(pariah.getCard().getName()).isEqualTo("Rampaging Werewolf");
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new TormentedPariah());
        Permanent pariah = findPermanent(player1, "Tormented Pariah");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(pariah.isTransformed()).isTrue();
        assertThat(pariah.getCard().getName()).isEqualTo("Rampaging Werewolf");
    }

    // ===== Helpers =====

}
