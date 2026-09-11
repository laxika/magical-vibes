package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UnstableHulk.class)
class UnstableHulkTest extends BaseCardTest {

    @Test
    void turningFaceUpBoostsItGrantsTrampleAndSkipsNextTurn() {
        Permanent hulk = castFaceDown();

        turnFaceUp(hulk);
        harness.passBothPriorities();

        assertThat(hulk.isFaceDown()).isFalse();
        assertThat(hulk.getEffectivePower()).isEqualTo(8);
        assertThat(hulk.getEffectiveToughness()).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, hulk, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    void faceUpBoostAndTrampleWearOffAtEndOfTurn() {
        Permanent hulk = castFaceDown();

        turnFaceUp(hulk);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hulk.getEffectivePower()).isEqualTo(2);
        assertThat(hulk.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hulk, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new UnstableHulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent hulk = findPermanent(player1, "Unstable Hulk");
        assertThat(hulk.isFaceDown()).isTrue();
        assertThat(hulk.getEffectivePower()).isEqualTo(2);
        assertThat(hulk.getEffectiveToughness()).isEqualTo(2);
        return hulk;
    }

    private void turnFaceUp(Permanent hulk) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hulk));
    }
}
