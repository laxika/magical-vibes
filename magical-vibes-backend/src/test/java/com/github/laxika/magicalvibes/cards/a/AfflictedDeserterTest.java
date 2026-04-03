package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WerewolfRansacker;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndDamageControllerIfDestroyedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AfflictedDeserterTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Front face has correct effects configured")
    void frontFaceHasCorrectEffects() {
        AfflictedDeserter card = new AfflictedDeserter();

        assertThat(card.getActivatedAbilities()).isEmpty();

        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect conditional =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("WerewolfRansacker");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        AfflictedDeserter card = new AfflictedDeserter();
        WerewolfRansacker backFace = (WerewolfRansacker) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).isEmpty();

        // Transform-back trigger
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);

        // Transform-into trigger: MayEffect wrapping destroy + damage
        assertThat(backFace.getEffects(EffectSlot.ON_TRANSFORM_TO_BACK_FACE)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.ON_TRANSFORM_TO_BACK_FACE).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) backFace.getEffects(EffectSlot.ON_TRANSFORM_TO_BACK_FACE).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(DestroyTargetPermanentAndDamageControllerIfDestroyedEffect.class);
        DestroyTargetPermanentAndDamageControllerIfDestroyedEffect destroyEffect =
                (DestroyTargetPermanentAndDamageControllerIfDestroyedEffect) mayEffect.wrapped();
        assertThat(destroyEffect.damage()).isEqualTo(3);
    }

    // ===== Werewolf transform: front -> back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Werewolf Ransacker when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability → transforms

        assertThat(deserter.isTransformed()).isTrue();
        assertThat(deserter.getCard().getName()).isEqualTo("Werewolf Ransacker");
        assertThat(gqs.getEffectivePower(gd, deserter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, deserter)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(deserter.isTransformed()).isFalse();
        assertThat(deserter.getCard().getName()).isEqualTo("Afflicted Deserter");
    }

    // ===== Transform trigger: destroy artifact + damage =====

    @Test
    @DisplayName("Transform trigger destroys artifact and deals 3 damage to its controller")
    void transformTriggerDestroysArtifactAndDealsDamage() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");
        Permanent ornithopter = findPermanent(player2, "Ornithopter");
        int player2LifeBefore = gd.getLife(player2.getId());

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, transform trigger goes on stack
        harness.passBothPriorities(); // resolve transform → transforms + MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → prompts may choice

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        // Choose the artifact to destroy
        harness.handlePermanentChosen(player1, ornithopter.getId());

        // Ornithopter should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .noneMatch(p -> p.getCard().getName().equals("Ornithopter"))).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"));

        // Player 2 should take 3 damage
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 3);

        // Deserter should still be transformed
        assertThat(deserter.isTransformed()).isTrue();
        assertThat(deserter.getCard().getName()).isEqualTo("Werewolf Ransacker");
    }

    @Test
    @DisplayName("Transform trigger can be declined")
    void transformTriggerCanBeDeclined() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");
        int player2LifeBefore = gd.getLife(player2.getId());

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, transform trigger goes on stack
        harness.passBothPriorities(); // resolve transform → transforms + MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → prompts may choice

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // Ornithopter should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Ornithopter"))).isTrue();

        // Player 2 should not take damage
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);

        // Deserter should still be transformed
        assertThat(deserter.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("No damage when indestructible artifact is targeted")
    void noDamageWhenIndestructibleArtifact() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        harness.addToBattlefield(player2, new DarksteelRelic());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");
        Permanent relic = findPermanent(player2, "Darksteel Relic");
        int player2LifeBefore = gd.getLife(player2.getId());

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, transform trigger goes on stack
        harness.passBothPriorities(); // resolve transform → transforms + MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → prompts may choice

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        // Choose the indestructible artifact
        harness.handlePermanentChosen(player1, relic.getId());

        // Darksteel Relic should still be on the battlefield (indestructible)
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Darksteel Relic"))).isTrue();

        // Player 2 should NOT take damage (artifact was not put into a graveyard)
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
    }

    // ===== Werewolf transform: back -> front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Werewolf Ransacker transforms back when a player cast two or more spells last turn")
    void werewolfTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");

        // Transform to Werewolf Ransacker first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, transform trigger on stack
        harness.passBothPriorities(); // resolve transform → MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → prompts may choice
        harness.handleMayAbilityChosen(player1, false); // decline (no artifacts anyway)
        assertThat(deserter.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(deserter.isTransformed()).isFalse();
        assertThat(deserter.getCard().getName()).isEqualTo("Afflicted Deserter");
        assertThat(gqs.getEffectivePower(gd, deserter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, deserter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Werewolf Ransacker does not transform back when only one spell was cast last turn")
    void werewolfDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");

        // Transform to Werewolf Ransacker first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, transform trigger on stack
        harness.passBothPriorities(); // resolve transform → MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → prompts may choice
        harness.handleMayAbilityChosen(player1, false); // decline (no artifacts anyway)
        assertThat(deserter.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(deserter.isTransformed()).isTrue();
        assertThat(deserter.getCard().getName()).isEqualTo("Werewolf Ransacker");
    }

    // ===== Transform trigger does not fire when no artifacts on battlefield =====

    @Test
    @DisplayName("Transform trigger does not prompt when no artifacts are on the battlefield")
    void transformTriggerNoArtifacts() {
        harness.addToBattlefield(player1, new AfflictedDeserter());
        Permanent deserter = findPermanent(player1, "Afflicted Deserter");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, transform trigger goes on stack
        harness.passBothPriorities(); // resolve transform → transforms + MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → prompts may choice

        // Accept — but no valid targets, so ability has no effect
        harness.handleMayAbilityChosen(player1, true);

        // Deserter should still be transformed
        assertThat(deserter.isTransformed()).isTrue();
        assertThat(deserter.getCard().getName()).isEqualTo("Werewolf Ransacker");
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
