package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new PhantasmalForm()));
        addMana();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
