package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WildbloodPack;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InstigatorGangTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        InstigatorGang card = new InstigatorGang();

        // No activated abilities
        assertThat(card.getActivatedAbilities()).isEmpty();

        // Static boost: Attacking creatures you control get +1/+0 (OWN_CREATURES + SELF)
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        StaticBoostEffect othersBoost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(othersBoost.powerBoost()).isEqualTo(1);
        assertThat(othersBoost.toughnessBoost()).isEqualTo(0);
        assertThat(othersBoost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(othersBoost.filter()).isInstanceOf(PermanentIsAttackingPredicate.class);
        StaticBoostEffect selfBoost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(selfBoost.powerBoost()).isEqualTo(1);
        assertThat(selfBoost.scope()).isEqualTo(GrantScope.SELF);
        assertThat(selfBoost.filter()).isInstanceOf(PermanentIsAttackingPredicate.class);

        // Each-upkeep transform trigger
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect conditional =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        // Back face exists
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("WildbloodPack");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        InstigatorGang card = new InstigatorGang();
        WildbloodPack backFace = (WildbloodPack) card.getBackFaceCard();

        // No activated abilities
        assertThat(backFace.getActivatedAbilities()).isEmpty();

        // Static boost: Attacking creatures you control get +3/+0 (OWN_CREATURES + SELF)
        assertThat(backFace.getEffects(EffectSlot.STATIC)).hasSize(2);
        StaticBoostEffect othersBoost = (StaticBoostEffect) backFace.getEffects(EffectSlot.STATIC).get(0);
        assertThat(othersBoost.powerBoost()).isEqualTo(3);
        assertThat(othersBoost.toughnessBoost()).isEqualTo(0);
        assertThat(othersBoost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(othersBoost.filter()).isInstanceOf(PermanentIsAttackingPredicate.class);
        StaticBoostEffect selfBoost = (StaticBoostEffect) backFace.getEffects(EffectSlot.STATIC).get(1);
        assertThat(selfBoost.powerBoost()).isEqualTo(3);
        assertThat(selfBoost.scope()).isEqualTo(GrantScope.SELF);
        assertThat(selfBoost.filter()).isInstanceOf(PermanentIsAttackingPredicate.class);

        // Each-upkeep transform trigger
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    // ===== Werewolf transform: front → back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Wildblood Pack when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");

        // spellsCastLastTurn is empty (no spells cast)
        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(gang.isTransformed()).isTrue();
        assertThat(gang.getCard().getName()).isEqualTo("Wildblood Pack");
        assertThat(gqs.getEffectivePower(gd, gang)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gang)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");

        // Simulate that a spell was cast last turn
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(gang.isTransformed()).isFalse();
        assertThat(gang.getCard().getName()).isEqualTo("Instigator Gang");
    }

    // ===== Werewolf transform: back → front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Wildblood Pack transforms back when a player cast two or more spells last turn")
    void wildbloodTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");

        // Transform to Wildblood Pack first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep
        harness.passBothPriorities(); // resolve transform
        assertThat(gang.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(gang.isTransformed()).isFalse();
        assertThat(gang.getCard().getName()).isEqualTo("Instigator Gang");
        assertThat(gqs.getEffectivePower(gd, gang)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gang)).isEqualTo(3);
    }

    @Test
    @DisplayName("Wildblood Pack does not transform back when only one spell was cast last turn")
    void wildbloodDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");

        // Transform to Wildblood Pack first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gang.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(gang.isTransformed()).isTrue();
        assertThat(gang.getCard().getName()).isEqualTo("Wildblood Pack");
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");

        // No spells cast last turn
        gd.spellsCastLastTurn.clear();

        // Trigger on opponent's upkeep (not player1's)
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(gang.isTransformed()).isTrue();
        assertThat(gang.getCard().getName()).isEqualTo("Wildblood Pack");
    }

    // ===== Static boost: attacking creatures you control get +1/+0 (front face) =====

    @Test
    @DisplayName("Attacking creatures you control get +1/+0 from front face")
    void frontFaceBoostsAttackingCreatures() {
        Permanent gang = addCreatureReady(player1, new InstigatorGang());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        declareAttackers(player1, List.of(0, 1));

        // Instigator Gang (2/3) attacking gets +1/+0 from its own static effect = 3/3
        assertThat(gqs.getEffectivePower(gd, gang)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gang)).isEqualTo(3);

        // Grizzly Bears (2/2) attacking gets +1/+0 = 3/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-attacking creatures do not get the boost")
    void nonAttackingCreaturesNotBoosted() {
        addCreatureReady(player1, new InstigatorGang());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        // Only attack with Instigator Gang (index 0), not bears
        declareAttackers(player1, List.of(0));

        // Bears is not attacking, should remain 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's attacking creatures do not get the boost")
    void opponentAttackingCreaturesNotBoosted() {
        addCreatureReady(player1, new InstigatorGang());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        declareAttackers(player2, List.of(0));

        // Opponent's bears attacking should not get the +1/+0
        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(2);
    }

    // ===== Static boost: attacking creatures you control get +3/+0 (back face) =====

    @Test
    @DisplayName("Attacking creatures you control get +3/+0 from back face")
    void backFaceBoostsAttackingCreatures() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");
        gang.setSummoningSick(false);

        // Transform to Wildblood Pack
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep
        harness.passBothPriorities(); // resolve transform
        assertThat(gang.isTransformed()).isTrue();

        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        declareAttackers(player1, List.of(0, 1));

        // Wildblood Pack (5/5) attacking gets +3/+0 from its own static effect = 8/5
        assertThat(gqs.getEffectivePower(gd, gang)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, gang)).isEqualTo(5);

        // Grizzly Bears (2/2) attacking gets +3/+0 = 5/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
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

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
