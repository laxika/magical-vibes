package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UlvenwaldMysticsTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Front face has correct effects configured")
    void frontFaceHasCorrectEffects() {
        UlvenwaldMystics card = new UlvenwaldMystics();

        // No activated abilities on front face
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
        assertThat(card.getBackFaceClassName()).isEqualTo("UlvenwaldPrimordials");
    }

    @Test
    @DisplayName("Back face has correct effects and abilities configured")
    void backFaceHasCorrectEffects() {
        UlvenwaldMystics card = new UlvenwaldMystics();
        UlvenwaldPrimordials backFace = (UlvenwaldPrimordials) card.getBackFaceCard();

        // Regeneration activated ability
        assertThat(backFace.getActivatedAbilities()).hasSize(1);
        assertThat(backFace.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{G}");
        assertThat(backFace.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(backFace.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(backFace.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);

        // Each-upkeep transform trigger
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    // ===== Werewolf transform: front → back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Ulvenwald Primordials when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new UlvenwaldMystics());
        Permanent mystics = findPermanent(player1, "Ulvenwald Mystics");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(mystics.isTransformed()).isTrue();
        assertThat(mystics.getCard().getName()).isEqualTo("Ulvenwald Primordials");
        assertThat(gqs.getEffectivePower(gd, mystics)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mystics)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new UlvenwaldMystics());
        Permanent mystics = findPermanent(player1, "Ulvenwald Mystics");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(mystics.isTransformed()).isFalse();
        assertThat(mystics.getCard().getName()).isEqualTo("Ulvenwald Mystics");
    }

    // ===== Werewolf transform: back → front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Ulvenwald Primordials transforms back when a player cast two or more spells last turn")
    void primordialTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new UlvenwaldMystics());
        Permanent mystics = findPermanent(player1, "Ulvenwald Mystics");

        // Transform to Ulvenwald Primordials first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep
        harness.passBothPriorities(); // resolve transform
        assertThat(mystics.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(mystics.isTransformed()).isFalse();
        assertThat(mystics.getCard().getName()).isEqualTo("Ulvenwald Mystics");
        assertThat(gqs.getEffectivePower(gd, mystics)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mystics)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ulvenwald Primordials does not transform back when only one spell was cast last turn")
    void primordialDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new UlvenwaldMystics());
        Permanent mystics = findPermanent(player1, "Ulvenwald Mystics");

        // Transform to Ulvenwald Primordials first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(mystics.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(mystics.isTransformed()).isTrue();
        assertThat(mystics.getCard().getName()).isEqualTo("Ulvenwald Primordials");
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new UlvenwaldMystics());
        Permanent mystics = findPermanent(player1, "Ulvenwald Mystics");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(mystics.isTransformed()).isTrue();
        assertThat(mystics.getCard().getName()).isEqualTo("Ulvenwald Primordials");
    }

    // ===== Back face regeneration ability =====

    @Test
    @DisplayName("Ulvenwald Primordials can activate regeneration ability")
    void primordialCanActivateRegeneration() {
        harness.addToBattlefield(player1, new UlvenwaldMystics());
        Permanent mystics = findPermanent(player1, "Ulvenwald Mystics");

        // Transform to Ulvenwald Primordials
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(mystics.isTransformed()).isTrue();

        // Activate regeneration ability
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities(); // resolve

        assertThat(mystics.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Ulvenwald Primordials from lethal damage")
    void regenerationSavesPrimordialFromLethalDamage() {
        harness.addToBattlefield(player1, new UlvenwaldMystics());
        Permanent mystics = findPermanent(player1, "Ulvenwald Mystics");

        // Transform to Ulvenwald Primordials (5/5)
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(mystics.isTransformed()).isTrue();

        // Give it a regeneration shield and set up as blocker
        mystics.setRegenerationShield(1);
        mystics.setSummoningSick(false);
        mystics.setBlocking(true);
        mystics.addBlockingTarget(0);

        // Create a 6/6 attacker (power > 5 toughness to deal lethal)
        UlvenwaldMystics attackerCard = new UlvenwaldMystics();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setPowerModifier(3); // 3+3=6 power
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Ulvenwald Primordials should survive via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ulvenwald Primordials"));
        assertThat(mystics.isTapped()).isTrue();
        assertThat(mystics.getRegenerationShield()).isEqualTo(0);
    }

    // ===== Helpers =====

}
