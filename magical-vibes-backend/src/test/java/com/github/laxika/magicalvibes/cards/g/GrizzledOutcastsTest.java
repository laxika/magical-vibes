package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KrallenhordeWantons;
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

class GrizzledOutcastsTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        GrizzledOutcasts card = new GrizzledOutcasts();

        assertThat(card.getActivatedAbilities()).isEmpty();

        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect conditional =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("KrallenhordeWantons");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        GrizzledOutcasts card = new GrizzledOutcasts();
        KrallenhordeWantons backFace = (KrallenhordeWantons) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).isEmpty();

        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    // ===== Werewolf transform: front -> back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Krallenhorde Wantons when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new GrizzledOutcasts());
        Permanent outcasts = findPermanent(player1, "Grizzled Outcasts");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(outcasts.isTransformed()).isTrue();
        assertThat(outcasts.getCard().getName()).isEqualTo("Krallenhorde Wantons");
        assertThat(gqs.getEffectivePower(gd, outcasts)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, outcasts)).isEqualTo(7);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new GrizzledOutcasts());
        Permanent outcasts = findPermanent(player1, "Grizzled Outcasts");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(outcasts.isTransformed()).isFalse();
        assertThat(outcasts.getCard().getName()).isEqualTo("Grizzled Outcasts");
    }

    // ===== Werewolf transform: back -> front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Krallenhorde Wantons transforms back when a player cast two or more spells last turn")
    void wantonsTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new GrizzledOutcasts());
        Permanent outcasts = findPermanent(player1, "Grizzled Outcasts");

        // Transform to Krallenhorde Wantons first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outcasts.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(outcasts.isTransformed()).isFalse();
        assertThat(outcasts.getCard().getName()).isEqualTo("Grizzled Outcasts");
        assertThat(gqs.getEffectivePower(gd, outcasts)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, outcasts)).isEqualTo(4);
    }

    @Test
    @DisplayName("Krallenhorde Wantons does not transform back when only one spell was cast last turn")
    void wantonsDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new GrizzledOutcasts());
        Permanent outcasts = findPermanent(player1, "Grizzled Outcasts");

        // Transform to Krallenhorde Wantons first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outcasts.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(outcasts.isTransformed()).isTrue();
        assertThat(outcasts.getCard().getName()).isEqualTo("Krallenhorde Wantons");
    }

    // ===== Transform triggers on every upkeep =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new GrizzledOutcasts());
        Permanent outcasts = findPermanent(player1, "Grizzled Outcasts");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(outcasts.isTransformed()).isTrue();
        assertThat(outcasts.getCard().getName()).isEqualTo("Krallenhorde Wantons");
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
