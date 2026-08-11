package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrayMerchantOfAsphodel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MogissMarauderTest extends BaseCardTest {

    @Test
    void grantsKeywordsToUpToBlackDevotionTargetsIncludingItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new MogissMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(first.hasKeyword(Keyword.INTIMIDATE)).isTrue();
        assertThat(second.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(second.hasKeyword(Keyword.INTIMIDATE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(first.hasKeyword(Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    void grantsToTwoTargetsWhenBlackDevotionIsTwo() {
        harness.addToBattlefield(player1, new GrayMerchantOfAsphodel());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new MogissMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(second.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(third.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(first.hasKeyword(Keyword.INTIMIDATE)).isTrue();
        assertThat(second.hasKeyword(Keyword.INTIMIDATE)).isTrue();
        assertThat(third.hasKeyword(Keyword.INTIMIDATE)).isFalse();
    }
}
