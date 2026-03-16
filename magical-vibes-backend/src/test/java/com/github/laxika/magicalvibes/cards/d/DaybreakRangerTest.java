package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NightfallPredator;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.StormfrontPegasus;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaybreakRangerTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        DaybreakRanger card = new DaybreakRanger();

        // One activated ability: {T}: deal 2 damage to target creature with flying
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .anyMatch(e -> e instanceof DealDamageToTargetCreatureEffect d && d.damage() == 2);

        // Each-upkeep transform trigger
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect conditional =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        // Back face exists
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("NightfallPredator");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        DaybreakRanger card = new DaybreakRanger();
        NightfallPredator backFace = (NightfallPredator) card.getBackFaceCard();

        // One activated ability: {R}, {T}: fight target creature
        assertThat(backFace.getActivatedAbilities()).hasSize(1);
        assertThat(backFace.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(backFace.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{R}");
        assertThat(backFace.getActivatedAbilities().getFirst().getEffects())
                .anyMatch(e -> e instanceof SourceFightsTargetCreatureEffect);

        // Each-upkeep transform trigger
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    // ===== Front face: {T}: deal 2 damage to target creature with flying =====

    @Test
    @DisplayName("Tap ability deals 2 damage to target creature with flying")
    void tapAbilityDeals2DamageToFlyingCreature() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        harness.addToBattlefield(player2, new StormfrontPegasus());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");
        Permanent pegasus = findPermanent(player2, "Stormfront Pegasus");
        ranger.setSummoningSick(false);

        int rangerIdx = indexOf(player1, ranger);
        harness.activateAbility(player1, rangerIdx, null, pegasus.getId());
        harness.passBothPriorities();

        // Stormfront Pegasus is 2/1, 2 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Stormfront Pegasus"));
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        harness.addToBattlefield(player2, new RuneclawBear());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");
        Permanent bear = findPermanent(player2, "Runeclaw Bear");
        ranger.setSummoningSick(false);

        int rangerIdx = indexOf(player1, ranger);

        assertThatThrownBy(() -> harness.activateAbility(player1, rangerIdx, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Werewolf transform: front → back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Nightfall Predator when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        // spellsCastLastTurn is empty (no spells cast)
        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(ranger.getCard().getName()).isEqualTo("Nightfall Predator");
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        // Simulate that a spell was cast last turn
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(ranger.isTransformed()).isFalse();
        assertThat(ranger.getCard().getName()).isEqualTo("Daybreak Ranger");
    }

    // ===== Werewolf transform: back → front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Nightfall Predator transforms back when a player cast two or more spells last turn")
    void nightfallTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        // Transform to Nightfall Predator first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep
        harness.passBothPriorities(); // resolve transform
        assertThat(ranger.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(ranger.isTransformed()).isFalse();
        assertThat(ranger.getCard().getName()).isEqualTo("Daybreak Ranger");
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nightfall Predator does not transform back when only one spell was cast last turn")
    void nightfallDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        // Transform to Nightfall Predator first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(ranger.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(ranger.getCard().getName()).isEqualTo("Nightfall Predator");
    }

    // ===== Back face: {R}, {T}: fight target creature =====

    @Test
    @DisplayName("Nightfall Predator fights target creature")
    void nightfallPredatorFightsTargetCreature() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        harness.addToBattlefield(player2, new RuneclawBear());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");
        Permanent bear = findPermanent(player2, "Runeclaw Bear");

        // Transform to Nightfall Predator
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(ranger.isTransformed()).isTrue();

        // Untap and remove summoning sickness for fight
        ranger.untap();
        ranger.setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 1);

        int rangerIdx = indexOf(player1, ranger);
        harness.activateAbility(player1, rangerIdx, null, bear.getId());
        harness.passBothPriorities();

        // Nightfall Predator is 4/4, Runeclaw Bear is 2/2
        // Bear takes 4 damage → dies. Predator takes 2 damage → survives (4 toughness - 2 = 2 left).
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Runeclaw Bear"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nightfall Predator"));
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new DaybreakRanger());
        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        // No spells cast last turn
        gd.spellsCastLastTurn.clear();

        // Trigger on opponent's upkeep (not player1's)
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(ranger.getCard().getName()).isEqualTo("Nightfall Predator");
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
