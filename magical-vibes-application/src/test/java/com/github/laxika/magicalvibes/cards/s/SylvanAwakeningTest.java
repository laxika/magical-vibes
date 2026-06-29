package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SylvanAwakeningTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has AnimateLandEffect with OWN_LANDS scope and UNTIL_YOUR_NEXT_TURN duration")
    void hasCorrectEffect() {
        SylvanAwakening card = new SylvanAwakening();

        var effects = card.getEffects(EffectSlot.SPELL);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(AnimateLandEffect.class);

        AnimateLandEffect effect = (AnimateLandEffect) effects.getFirst();
        assertThat(effect.power()).isEqualTo(2);
        assertThat(effect.toughness()).isEqualTo(2);
        assertThat(effect.grantedSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(effect.grantedKeywords()).containsExactlyInAnyOrder(
                Keyword.REACH, Keyword.INDESTRUCTIBLE, Keyword.HASTE
        );
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_LANDS);
        assertThat(effect.duration()).isEqualTo(EffectDuration.UNTIL_YOUR_NEXT_TURN);
    }

    // ===== Resolution: lands become creatures =====

    @Test
    @DisplayName("Casting Sylvan Awakening animates all lands you control as 2/2 Elemental creatures")
    void animatesAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElse(null);
        assertThat(forest).isNotNull();
        assertThat(forest.isAnimatedUntilNextTurn()).isTrue();
        assertThat(forest.getUntilNextTurnAnimatedPower()).isEqualTo(2);
        assertThat(forest.getUntilNextTurnAnimatedToughness()).isEqualTo(2);
        assertThat(forest.getEffectivePower()).isEqualTo(2);
        assertThat(forest.getEffectiveToughness()).isEqualTo(2);
        assertThat(forest.getUntilNextTurnSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(forest.getUntilNextTurnKeywords()).containsExactlyInAnyOrder(
                Keyword.REACH, Keyword.INDESTRUCTIBLE, Keyword.HASTE
        );

        Permanent mountain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .findFirst().orElse(null);
        assertThat(mountain).isNotNull();
        assertThat(mountain.isAnimatedUntilNextTurn()).isTrue();
        assertThat(mountain.getEffectivePower()).isEqualTo(2);
        assertThat(mountain.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated lands are treated as creatures")
    void animatedLandsAreCreatures() {
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElse(null);
        assertThat(forest).isNotNull();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Animated lands have reach, indestructible, and haste keywords")
    void animatedLandsHaveKeywords() {
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElse(null);
        assertThat(forest).isNotNull();
        assertThat(forest.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(forest.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(forest.hasKeyword(Keyword.HASTE)).isTrue();
    }

    // ===== Does not affect non-lands or opponent's lands =====

    @Test
    @DisplayName("Does not animate non-land permanents")
    void doesNotAnimateNonLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.isAnimatedUntilNextTurn()).isFalse();
    }

    @Test
    @DisplayName("Does not animate opponent's lands")
    void doesNotAnimateOpponentLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent opponentForest = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElse(null);
        assertThat(opponentForest).isNotNull();
        assertThat(opponentForest.isAnimatedUntilNextTurn()).isFalse();
    }

    // ===== Duration: survives end of turn, cleared at next turn =====

    @Test
    @DisplayName("Animation survives end-of-turn cleanup (resetModifiers)")
    void animationSurvivesEndOfTurn() {
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElse(null);
        assertThat(forest).isNotNull();

        // Simulate end-of-turn cleanup
        forest.resetModifiers();

        // Animation should still be present
        assertThat(forest.isAnimatedUntilNextTurn()).isTrue();
        assertThat(forest.getUntilNextTurnAnimatedPower()).isEqualTo(2);
        assertThat(forest.getUntilNextTurnAnimatedToughness()).isEqualTo(2);
        assertThat(forest.getUntilNextTurnKeywords()).containsExactlyInAnyOrder(
                Keyword.REACH, Keyword.INDESTRUCTIBLE, Keyword.HASTE
        );
        assertThat(forest.getUntilNextTurnSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Animation is cleared at beginning of controller's next turn")
    void animationClearedAtNextTurn() {
        harness.addToBattlefield(player1, new Forest());

        // Directly set up animation state to test clearUntilNextTurnEffects
        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElse(null);
        assertThat(forest).isNotNull();

        forest.setAnimatedUntilNextTurn(true);
        forest.setUntilNextTurnAnimatedPower(2);
        forest.setUntilNextTurnAnimatedToughness(2);
        forest.getUntilNextTurnSubtypes().add(CardSubtype.ELEMENTAL);
        forest.getUntilNextTurnKeywords().addAll(java.util.Set.of(Keyword.REACH, Keyword.INDESTRUCTIBLE, Keyword.HASTE));

        // Advance to player1's next turn — this should clear the animation
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // end player2's turn → advance to player1's turn

        GameData gd = harness.getGameData();

        forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElse(null);
        assertThat(forest).isNotNull();
        assertThat(forest.isAnimatedUntilNextTurn()).isFalse();
        assertThat(forest.getUntilNextTurnKeywords()).isEmpty();
        assertThat(forest.getUntilNextTurnSubtypes()).isEmpty();
    }

    // ===== Lands are still lands =====

    @Test
    @DisplayName("Animated lands are still lands (retain land type)")
    void animatedLandsAreStillLands() {
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElse(null);
        assertThat(forest).isNotNull();
        // Land type is preserved from the card itself
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }
}
