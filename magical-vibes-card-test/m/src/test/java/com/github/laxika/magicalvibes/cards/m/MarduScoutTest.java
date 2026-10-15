package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarduScout.class})
class MarduScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast does not grant haste or return the creature at end step")
    void normalCastDoesNotUseDash() {
        harness.setHand(player1, List.of(new MarduScout()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent scout = findPermanent(player1, "Mardu Scout");
        assertThat(scout.hasKeyword(Keyword.HASTE)).isFalse();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(findPermanent(player1, "Mardu Scout")).isSameAs(scout);
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new MarduScout()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        Permanent scout = findPermanent(player1, "Mardu Scout");
        assertThat(scout.hasKeyword(Keyword.HASTE)).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Mardu Scout");
        harness.assertNotOnBattlefield(player1, "Mardu Scout");
    }

    @Test
    @DisplayName("Dash establishes its return without an enters-the-battlefield trigger")
    void dashDoesNotCreateAnEntryTrigger() {
        harness.setHand(player1, List.of(new MarduScout()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mardu Scout");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dash returns the creature only when its end-step trigger resolves")
    void dashReturnUsesTheStack() {
        harness.setHand(player1, List.of(new MarduScout()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Mardu Scout");
        harness.assertNotInHand(player1, "Mardu Scout");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertInHand(player1, "Mardu Scout");
        harness.assertNotOnBattlefield(player1, "Mardu Scout");
    }

    @Test
    @DisplayName("A creature cast with dash can attack the turn it enters")
    void dashedScoutCanAttackImmediately() {
        harness.setHand(player1, List.of(new MarduScout()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(findPermanent(player1, "Mardu Scout").isAttacking()).isTrue();
    }
}
