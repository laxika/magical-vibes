package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SunsetPyramidTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three brick counters")
    void entersWithThreeBrickCounters() {
        harness.setHand(player1, List.of(new SunsetPyramid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent pyramid = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sunset Pyramid"))
                .findFirst().orElseThrow();
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
}
