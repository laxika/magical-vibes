package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrayMerchantOfAsphodel;
import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MogissMarauder.class, GrayMerchantOfAsphodel.class, NessianCourser.class, HerosDownfall.class})
class MogissMarauderTest extends BaseCardTest {

    @Test
    void grantsKeywordsToAnOpposingCreatureUntilEndOfTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NessianCourser());

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
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(first.hasKeyword(Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    void canChooseFewerTargetsThanBlackDevotion() {
        harness.addToBattlefield(player1, new GrayMerchantOfAsphodel());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new NessianCourser());

        harness.setHand(player1, List.of(new MogissMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(second.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(third.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(first.hasKeyword(Keyword.INTIMIDATE)).isTrue();
        assertThat(second.hasKeyword(Keyword.INTIMIDATE)).isTrue();
        assertThat(third.hasKeyword(Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    void canTargetItselfAndCountsItsOwnBlackManaSymbol() {
        harness.addToBattlefield(player2, new GrayMerchantOfAsphodel());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        castMarauder();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Mogis's Marauder"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        Permanent marauder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof MogissMarauder).findFirst().orElseThrow();
        assertThat(marauder.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(marauder.hasKeyword(Keyword.INTIMIDATE)).isTrue();
        assertThat(other.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(other.hasKeyword(Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    void canChooseZeroTargets() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        castMarauder();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(other.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(other.hasKeyword(Keyword.INTIMIDATE)).isFalse();
        Permanent marauder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof MogissMarauder).findFirst().orElseThrow();
        assertThat(marauder.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(marauder.hasKeyword(Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    void chosenTargetsStillGainBothKeywordsAfterSourceDiesAndDevotionFalls() {
        harness.addToBattlefield(player1, new GrayMerchantOfAsphodel());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        castMarauder();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));

        harness.setHand(player2, List.of(new HerosDownfall()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Mogis's Marauder"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof MogissMarauder);
        harness.passBothPriorities();

        for (Permanent target : List.of(first, second, third)) {
            assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
            assertThat(target.hasKeyword(Keyword.INTIMIDATE)).isTrue();
        }
    }

    @Test
    void remainingLegalTargetGainsBothKeywordsWhenAnotherTargetDies() {
        harness.addToBattlefield(player1, new GrayMerchantOfAsphodel());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        castMarauder();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        harness.setHand(player2, List.of(new HerosDownfall()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        harness.passBothPriorities();

        assertThat(second.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(second.hasKeyword(Keyword.INTIMIDATE)).isTrue();
    }

    private void castMarauder() {
        harness.setHand(player1, List.of(new MogissMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
