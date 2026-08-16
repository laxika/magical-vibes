package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeroicInterventionTest extends BaseCardTest {

    @Test
    void grantsHexproofAndIndestructibleToAllOwnPermanents() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new HeroicIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertHasBothKeywords(ownCreature);
        assertHasBothKeywords(ownLand);
        assertThat(opponentCreature.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(opponentLand.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(opponentLand.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void grantsWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeroicIntervention()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertHasBothKeywords(ownCreature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(ownCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void assertHasBothKeywords(Permanent permanent) {
        assertThat(permanent.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(permanent.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
