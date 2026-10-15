package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({MarduStrikeLeader.class})
class MarduStrikeLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a 2/1 black Warrior token")
    void attackingCreatesWarriorToken() {
        addCreatureReady(player1, new MarduStrikeLeader());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new MarduStrikeLeader()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent leader = findPermanent(player1, "Mardu Strike Leader");
        assertThat(leader.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Mardu Strike Leader");
        harness.assertNotOnBattlefield(player1, "Mardu Strike Leader");
    }

    @Test
    @DisplayName("Normal casting does not grant haste or return the leader at end step")
    void normalCastingDoesNotUseDash() {
        harness.setHand(player1, List.of(new MarduStrikeLeader()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent leader = findPermanent(player1, "Mardu Strike Leader");
        assertThat(leader.hasKeyword(Keyword.HASTE)).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mardu Strike Leader")).isSameAs(leader);
        harness.assertNotInHand(player1, "Mardu Strike Leader");
    }

    @Test
    @DisplayName("Dash does not create an enters-the-battlefield triggered ability")
    void dashDoesNotCreateAnEtbTrigger() {
        harness.setHand(player1, List.of(new MarduStrikeLeader()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mardu Strike Leader");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dash creates exactly one return trigger and leaves the Warrior token")
    void dashReturnUsesOneTriggerAndLeavesTheToken() {
        harness.setHand(player1, List.of(new MarduStrikeLeader()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        resolveAllTriggers();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Mardu Strike Leader");
        harness.assertNotInHand(player1, "Mardu Strike Leader");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertInHand(player1, "Mardu Strike Leader");
        harness.assertNotOnBattlefield(player1, "Mardu Strike Leader");
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
    }
}
