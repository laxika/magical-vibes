package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GenasiRabbleRouser.class, GrizzlyBears.class})
class GenasiRabbleRouserTest extends BaseCardTest {

    @Test
    void doubleTeamConjuresDuplicateAndRemovesKeyword() {
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
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentRabbleRouser = addCreatureReady(player2, new GenasiRabbleRouser());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rabbleRouser)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherRabbleRouser)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentRabbleRouser)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rabbleRouser)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherRabbleRouser)).isEqualTo(1);
    }
}
