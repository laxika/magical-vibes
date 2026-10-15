package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PressTheAdvantage.class, ColossodonYearling.class, Mountain.class})
class PressTheAdvantageTest extends BaseCardTest {

    @Test
    @DisplayName("Gives up to two target creatures +2/+2 and trample")
    void boostsTwoTargetsAndGrantsTrample() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        cast(List.of(first.getId(), second.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(first.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        assertThat(second.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Allows one target or no targets")
    void allowsFewerThanTwoTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        cast(List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.setHand(player1, List.of(new PressTheAdvantage()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Press the Advantage");
    }

    @Test
    @DisplayName("The boost and trample wear off at cleanup")
    void wearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        cast(List.of(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new PressTheAdvantage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the same creature twice")
    void cannotChooseDuplicateTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        harness.setHand(player1, List.of(new PressTheAdvantage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new PressTheAdvantage()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still affects the remaining target when the other leaves the battlefield")
    void resolvesForRemainingTarget() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        Permanent untouched = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        harness.setHand(player1, List.of(new PressTheAdvantage()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, List.of(removed.getId(), remaining.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, removed));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, remaining)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, remaining)).isEqualTo(6);
        assertThat(remaining.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, untouched)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, untouched)).isEqualTo(4);
        assertThat(untouched.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Press the Advantage");
    }

    private void cast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new PressTheAdvantage()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
