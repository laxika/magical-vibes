package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScorchRider.class})
class ScorchRiderTest extends BaseCardTest {

    @Test
    void doesNotGainHasteWhenNotKicked() {
        harness.setHand(player1, List.of(new ScorchRider()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent rider = findPermanent(player1, "Scorch Rider");
        assertThat(gqs.hasKeyword(gd, rider, Keyword.HASTE)).isFalse();
    }

    @Test
    void kickedRiderGainsHasteUntilEndOfTurn() {
        harness.setHand(player1, List.of(new ScorchRider()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rider = findPermanent(player1, "Scorch Rider");
        assertThat(gqs.hasKeyword(gd, rider, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.HASTE)).isFalse();
    }
}
