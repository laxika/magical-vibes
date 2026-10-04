package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SoldiersOfTheWatch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenasiRabbleRouser.class, SoldiersOfTheWatch.class})
class GenasiRabbleRouserTest extends BaseCardTest {

    @Test
    void doubleTeamConjuresDuplicateAndRemovesKeyword() {
        harness.setHand(player1, List.of());
        Permanent rabbleRouser = addCreatureReady(player1, new GenasiRabbleRouser());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, rabbleRouser, Keyword.DOUBLE_TEAM)).isFalse();
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.getFirst().hasKeyword(Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void activationBoostsOnlyYourNamedCreaturesUntilEndOfTurn() {
        Permanent rabbleRouser = addCreatureReady(player1, new GenasiRabbleRouser());
        Permanent otherRabbleRouser = addCreatureReady(player1, new GenasiRabbleRouser());
        Permanent ownSoldiers = addCreatureReady(player1, new SoldiersOfTheWatch());
        Permanent opponentRabbleRouser = addCreatureReady(player2, new GenasiRabbleRouser());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rabbleRouser)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherRabbleRouser)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownSoldiers)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentRabbleRouser)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rabbleRouser)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherRabbleRouser)).isEqualTo(1);
    }

    @Test
    void repeatedActivationsStackWithoutTappingOrSummoningSicknessRestrictions() {
        Permanent rabbleRouser = addCreatureReady(player1, new GenasiRabbleRouser());
        rabbleRouser.setSummoningSick(true);
        rabbleRouser.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rabbleRouser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rabbleRouser)).isEqualTo(3);
    }

    @Test
    void boostIncludesCreaturesPresentAtResolutionButNotLaterArrivals() {
        Permanent source = addCreatureReady(player1, new GenasiRabbleRouser());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = addCreatureReady(player1, new GenasiRabbleRouser());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new GenasiRabbleRouser());

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(1);
    }

    @Test
    void originalDoesNotConjureAgainOnALaterAttack() {
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new GenasiRabbleRouser());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        advanceToUpkeep(player1);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
