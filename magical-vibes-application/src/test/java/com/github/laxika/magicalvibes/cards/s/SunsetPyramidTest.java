package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunsetPyramid.class, Forest.class})
class SunsetPyramidTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three brick counters")
    void entersWithThreeBrickCounters() {
        harness.setHand(player1, List.of(new SunsetPyramid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent pyramid = findPermanent(player1, "Sunset Pyramid");
        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(3);
    }

    @Test
    @DisplayName("{2}, {T}, Remove a brick counter: draws a card")
    void drawAbilityDrawsAndRemovesBrick() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new SunsetPyramid());
        pyramid.setCounterCount(CounterType.BRICK, 3);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(2);
        assertThat(pyramid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draw ability cannot activate with no brick counters")
    void drawAbilityRequiresBrickCounter() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new SunsetPyramid());
        pyramid.setCounterCount(CounterType.BRICK, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{2}, {T}: Scry 1")
    void scryAbilityEntersScryWithOneCard() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new SunsetPyramid());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        assertThat(pyramid.isTapped()).isTrue();
    }

    @Test
    void lastBrickIsPaidBeforeDrawResolves() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new SunsetPyramid());
        pyramid.setCounterCount(CounterType.BRICK, 1);
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isZero();
        assertThat(pyramid.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard).hasSize(handBefore + 1);
    }

    @Test
    void scryWorksWithoutBrickCounters() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new SunsetPyramid());
        pyramid.setCounterCount(CounterType.BRICK, 0);
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isZero();
    }

    @Test
    void neitherAbilityCanBeActivatedWhileTapped() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new SunsetPyramid());
        pyramid.setCounterCount(CounterType.BRICK, 3);
        pyramid.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void neitherAbilityCanBeActivatedWithOnlyOneMana() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new SunsetPyramid());
        pyramid.setCounterCount(CounterType.BRICK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pyramid.isTapped()).isFalse();
        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
