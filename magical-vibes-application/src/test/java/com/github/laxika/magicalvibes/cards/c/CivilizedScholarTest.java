package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoundByMoonsilver;
import com.github.laxika.magicalvibes.cards.n.NeglectedHeirloom;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CivilizedScholar.class, WalkingCorpse.class, ThinkTwice.class})
class CivilizedScholarTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability draws a card and forces a discard — non-creature does not transform")
    void discardingNonCreatureDoesNotTransform() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new CivilizedScholar());
        scholar.setSummoningSick(false);

        // Hand: one non-creature card (Think Twice = instant)
        harness.setHand(player1, List.of(new ThinkTwice()));
        // Library needs a card to draw
        harness.setLibrary(player1, List.of(new ThinkTwice()));

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
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new CivilizedScholar());
        scholar.setSummoningSick(false);

        // Hand: one creature card (Walking Corpse = creature)
        harness.setHand(player1, List.of(new WalkingCorpse()));
        // Library needs a card to draw
        harness.setLibrary(player1, List.of(new ThinkTwice()));

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
        harness.passUntil(TurnStep.END_STEP);

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
        harness.passUntil(TurnStep.END_STEP);

        // The trigger should NOT have fired (intervening-if: attacked this turn)
        assertThat(gd.stack).isEmpty();
        assertThat(brute.isTransformed()).isTrue();
        assertThat(brute.getCard().getName()).isEqualTo("Homicidal Brute");
        assertThat(brute.isTapped()).isFalse();
    }

    @Test
    @CardUsed({BoundByMoonsilver.class})
    @DisplayName("A creature discard untaps Scholar but cannot transform it under Bound by Moonsilver")
    void cannotTransformWhenEnchantedByBoundByMoonsilver() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new CivilizedScholar());
        scholar.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new BoundByMoonsilver());
        aura.setAttachedTo(scholar.getId());
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.setLibrary(player1, List.of(new ThinkTwice()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(scholar.isTapped()).isFalse();
        assertThat(scholar.isTransformed()).isFalse();
        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertInHand(player1, "Think Twice");
    }

    @Test
    @DisplayName("The creature just drawn can be discarded to transform Scholar")
    void discardingDrawnCreatureTransforms() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new CivilizedScholar());
        scholar.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new WalkingCorpse()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(scholar.isTapped()).isFalse();
        assertThat(scholar.isTransformed()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("An already tapped Brute still transforms at its controller's end step")
    void tappedBruteStillTransformsBack() {
        Permanent brute = createTransformedBrute(player1);
        brute.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(brute.isTapped()).isTrue();
        assertThat(brute.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Brute does not trigger during its opponent's end step")
    void opponentEndStepDoesNotTransformBrute() {
        Permanent brute = createTransformedBrute(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(brute.isTapped()).isFalse();
        assertThat(brute.isTransformed()).isTrue();
    }

    @Test
    @CardUsed({NeglectedHeirloom.class})
    @DisplayName("Scholar transforming triggers its attached Neglected Heirloom")
    void transformationTriggersAttachedEquipment() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new CivilizedScholar());
        scholar.setSummoningSick(false);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new NeglectedHeirloom());
        equipment.setAttachedTo(scholar.getId());
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.setLibrary(player1, List.of(new ThinkTwice()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(scholar.isTransformed()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(equipment.isTransformed()).isTrue();
        assertThat(equipment.getAttachedTo()).isEqualTo(scholar.getId());
    }

    /**
     * Creates a Civilized Scholar, transforms it to Homicidal Brute via the loot ability,
     * and returns the permanent.
     */
    private Permanent createTransformedBrute(Player player) {
        Permanent scholar = harness.addToBattlefieldAndReturn(player, new CivilizedScholar());
        scholar.setSummoningSick(false);

        harness.setHand(player, List.of(new WalkingCorpse()));
        harness.setLibrary(player, List.of(new ThinkTwice()));

        int scholarIdx = gd.playerBattlefields.get(player.getId()).indexOf(scholar);
        harness.activateAbility(player, scholarIdx, null, null);
        harness.passBothPriorities();

        List<Card> hand = gd.playerHands.get(player.getId());
        int creatureIdx = findCardIndexByType(hand, CardType.CREATURE);
        harness.handleCardChosen(player, creatureIdx);

        assertThat(scholar.isTransformed()).isTrue();
        return scholar;
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
