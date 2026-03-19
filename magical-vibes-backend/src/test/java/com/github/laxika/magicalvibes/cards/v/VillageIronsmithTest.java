package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.Ironfang;
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

class VillageIronsmithTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        VillageIronsmith card = new VillageIronsmith();

        assertThat(card.getActivatedAbilities()).isEmpty();

        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect conditional =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("Ironfang");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        VillageIronsmith card = new VillageIronsmith();
        Ironfang backFace = (Ironfang) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).isEmpty();

        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    // ===== Werewolf transform: front -> back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Ironfang when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(ironsmith.isTransformed()).isTrue();
        assertThat(ironsmith.getCard().getName()).isEqualTo("Ironfang");
        assertThat(gqs.getEffectivePower(gd, ironsmith)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ironsmith)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(ironsmith.isTransformed()).isFalse();
        assertThat(ironsmith.getCard().getName()).isEqualTo("Village Ironsmith");
    }

    // ===== Werewolf transform: back -> front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Ironfang transforms back when a player cast two or more spells last turn")
    void werewolfTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");

        // Transform to Ironfang first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(ironsmith.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(ironsmith.isTransformed()).isFalse();
        assertThat(ironsmith.getCard().getName()).isEqualTo("Village Ironsmith");
        assertThat(gqs.getEffectivePower(gd, ironsmith)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ironsmith)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ironfang does not transform back when only one spell was cast last turn")
    void werewolfDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");

        // Transform to Ironfang first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(ironsmith.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(ironsmith.isTransformed()).isTrue();
        assertThat(ironsmith.getCard().getName()).isEqualTo("Ironfang");
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(ironsmith.isTransformed()).isTrue();
        assertThat(ironsmith.getCard().getName()).isEqualTo("Ironfang");
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
