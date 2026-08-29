package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterspoutWarden.class, GrizzlyBears.class})
class WaterspoutWardenTest extends BaseCardTest {

    @Test
    void gainsFlyingWhenAnotherCreatureEnteredUnderYourControlThisTurn() {
        Permanent warden = addCreatureReady(player1, new WaterspoutWarden());
        recordEntry(player1.getId(), new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotGainFlyingWithoutAnotherCreatureEntry() {
        Permanent warden = addCreatureReady(player1, new WaterspoutWarden());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotCountOpponentOrItsOwnEntry() {
        Permanent warden = addCreatureReady(player1, new WaterspoutWarden());
        recordEntry(player2.getId(), new GrizzlyBears());
        recordEntry(player1.getId(), warden.getCard());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isFalse();
    }

    private void recordEntry(UUID controllerId, Card card) {
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                .add(card);
    }
}
