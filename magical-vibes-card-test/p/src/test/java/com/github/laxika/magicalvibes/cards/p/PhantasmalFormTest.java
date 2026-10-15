package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantasmalForm.class, GrizzlyBears.class, SerraAngel.class, Island.class, Mountain.class})
class PhantasmalFormTest extends BaseCardTest {

    @Test
    @DisplayName("Makes up to two creatures 3/3 blue Illusions with flying and draws a card")
    void transformsTwoCreaturesAndDraws() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setLibrary(player1, List.of(new Island()));

        cast(List.of(bear.getId(), angel.getId()));

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, bear, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, bear, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.ILLUSION)).isTrue();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.BLUE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(angel, CardSubtype.ANGEL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, angel, CardSubtype.ILLUSION)).isTrue();
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Can resolve without targets and still draws a card")
    void canResolveWithoutTargets() {
        harness.setLibrary(player1, List.of(new Island()));

        cast(List.of());

        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("The transformation wears off at end of turn")
    void transformationWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(bear.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasColor(gd, bear, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.ILLUSION)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new PhantasmalForm()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Still transforms the remaining legal target and draws when one target leaves")
    void resolvesWithOneRemainingTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new PhantasmalForm()));
        addMana();
        harness.castInstant(player1, 0, List.of(bear.getId(), angel.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bear));

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, angel, CardSubtype.ANGEL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, angel, CardSubtype.ILLUSION)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Does not draw when all chosen targets leave before resolution")
    void doesNotDrawWhenAllTargetsAreIllegal() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new PhantasmalForm()));
        addMana();
        harness.castInstant(player1, 0, List.of(bear.getId(), angel.getId()));
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, bear);
            harness.getPermanentRemovalService().removePermanentToHand(gd, angel);
        });

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Phantasmal Form");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sets base power and toughness while preserving counters and leaving other creatures unchanged")
    void preservesCountersAndOnlyTransformsChosenCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new Island()));

        cast(List.of(bear.getId()));

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, bear, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.ILLUSION)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBear, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasColor(gd, otherBear, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, otherBear, CardSubtype.ILLUSION)).isFalse();
        harness.assertInHand(player1, "Island");
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new PhantasmalForm()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
