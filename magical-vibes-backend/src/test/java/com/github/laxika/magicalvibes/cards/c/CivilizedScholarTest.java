package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HomicidalBrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DidntAttackConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardTransformIfCreatureDiscardedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CivilizedScholarTest extends BaseCardTest {

    // ===== Front face: Civilized Scholar =====

    @Test
    @DisplayName("Tap ability draws a card and forces a discard — non-creature does not transform")
    void discardingNonCreatureDoesNotTransform() {
        harness.addToBattlefield(player1, new CivilizedScholar());
        Permanent scholar = findPermanent(player1, "Civilized Scholar");
        scholar.setSummoningSick(false);

        // Hand: one non-creature card (Cancel = instant)
        harness.setHand(player1, List.of(new Cancel()));
        // Library needs a card to draw
        gd.playerDecks.get(player1.getId()).add(new Cancel());

        int scholarIdx = gd.playerBattlefields.get(player1.getId()).indexOf(scholar);
        harness.activateAbility(player1, scholarIdx, null, null);
        harness.passBothPriorities();

        // Discard the first card (index 0)
        harness.handleCardChosen(player1, 0);

        // Scholar should remain tapped and NOT transformed
        assertThat(scholar.isTapped()).isTrue();
        assertThat(scholar.isTransformed()).isFalse();
        assertThat(scholar.getCard().getName()).isEqualTo("Civilized Scholar");
    }

    @Test
    @DisplayName("Discarding a creature card untaps and transforms Civilized Scholar")
    void discardingCreatureUntapsAndTransforms() {
        harness.addToBattlefield(player1, new CivilizedScholar());
        Permanent scholar = findPermanent(player1, "Civilized Scholar");
        scholar.setSummoningSick(false);

        // Hand: one creature card (Canyon Minotaur = creature)
        harness.setHand(player1, List.of(new CanyonMinotaur()));
        // Library needs a card to draw
        gd.playerDecks.get(player1.getId()).add(new Cancel());

        int scholarIdx = gd.playerBattlefields.get(player1.getId()).indexOf(scholar);
        harness.activateAbility(player1, scholarIdx, null, null);
        harness.passBothPriorities();

        // After draw, we have 2 cards. Find and discard the creature.
        List<Card> hand = gd.playerHands.get(player1.getId());
        int creatureIdx = findCardIndexByType(hand, CardType.CREATURE);
        assertThat(creatureIdx).isGreaterThanOrEqualTo(0);

        harness.handleCardChosen(player1, creatureIdx);

        // Should have untapped and transformed to Homicidal Brute
        assertThat(scholar.isTapped()).isFalse();
        assertThat(scholar.isTransformed()).isTrue();
        assertThat(scholar.getCard().getName()).isEqualTo("Homicidal Brute");
        assertThat(gqs.getEffectivePower(gd, scholar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scholar)).isEqualTo(1);
    }

    // ===== Back face: Homicidal Brute =====

    @Test
    @DisplayName("Homicidal Brute taps and transforms back if it didn't attack this turn")
    void bruteTransformsBackIfDidntAttack() {
        // Set up a transformed Homicidal Brute
        Permanent brute = createTransformedBrute(player1);

        // Brute did NOT attack this turn — untap it for clean state
        brute.untap();

        // Advance to end step by setting step to POSTCOMBAT_MAIN and passing
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // End step trigger should have pushed onto stack, resolve it
        harness.passBothPriorities();

        // Should be tapped and transformed back to Civilized Scholar
        assertThat(brute.isTapped()).isTrue();
        assertThat(brute.isTransformed()).isFalse();
        assertThat(brute.getCard().getName()).isEqualTo("Civilized Scholar");
    }

    @Test
    @DisplayName("Homicidal Brute does not transform back if it attacked this turn")
    void bruteDoesNotTransformIfAttacked() {
        // Set up a transformed Homicidal Brute
        Permanent brute = createTransformedBrute(player1);

        // Brute attacked this turn
        brute.untap();
        brute.setAttackedThisTurn(true);

        // Advance to end step
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The trigger should NOT have fired (intervening-if: attacked this turn)
        assertThat(brute.isTransformed()).isTrue();
        assertThat(brute.getCard().getName()).isEqualTo("Homicidal Brute");
        assertThat(brute.isTapped()).isFalse();
    }

    // ===== Configuration tests =====

    @Test
    @DisplayName("Card has correct effects configured")
    void hasCorrectEffects() {
        CivilizedScholar card = new CivilizedScholar();

        // One activated ability on front face (tap to loot)
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .anyMatch(e -> e instanceof DrawDiscardTransformIfCreatureDiscardedEffect);

        // Back face should exist
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard()).isInstanceOf(HomicidalBrute.class);

        // Back face has end step trigger with conditional
        assertThat(card.getBackFaceCard().getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED))
                .anyMatch(e -> e instanceof DidntAttackConditionalEffect);
    }

    // ===== Helpers =====

    /**
     * Creates a Civilized Scholar, transforms it to Homicidal Brute via the loot ability,
     * and returns the permanent.
     */
    private Permanent createTransformedBrute(Player player) {
        harness.addToBattlefield(player, new CivilizedScholar());
        Permanent scholar = findPermanent(player, "Civilized Scholar");
        scholar.setSummoningSick(false);

        harness.setHand(player, List.of(new CanyonMinotaur()));
        gd.playerDecks.get(player.getId()).add(new Cancel());

        int scholarIdx = gd.playerBattlefields.get(player.getId()).indexOf(scholar);
        harness.activateAbility(player, scholarIdx, null, null);
        harness.passBothPriorities();

        List<Card> hand = gd.playerHands.get(player.getId());
        int creatureIdx = findCardIndexByType(hand, CardType.CREATURE);
        harness.handleCardChosen(player, creatureIdx);

        assertThat(scholar.isTransformed()).isTrue();
        return scholar;
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    private int findCardIndexByType(List<Card> hand, CardType type) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).hasType(type)) {
                return i;
            }
        }
        return -1;
    }
}
