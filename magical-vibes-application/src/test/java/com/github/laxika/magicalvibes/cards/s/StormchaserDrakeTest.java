package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.MassiveMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormchaserDrake.class, GiantGrowth.class, MassiveMight.class})
class StormchaserDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when its controller's spell targets it")
    void drawsWhenOwnSpellTargetsIt() {
        Permanent drake = addCreatureReady(player1, new StormchaserDrake());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveInstant(player1, 0, drake.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not draw when an opponent's spell targets it")
    void doesNotDrawWhenOpponentSpellTargetsIt() {
        Permanent drake = addCreatureReady(player1, new StormchaserDrake());
        harness.setHand(player1, List.of());

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, drake.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draw trigger resolves before the targeting spell")
    void drawsBeforeTargetingSpellResolves() {
        Permanent drake = addCreatureReady(player1, new StormchaserDrake());
        StormchaserDrake drawnCard = new StormchaserDrake();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new MassiveMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, drake.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Each separate spell targeting the Drake draws a card")
    void drawsForEachSeparateSpell() {
        Permanent drake = addCreatureReady(player1, new StormchaserDrake());
        StormchaserDrake firstDraw = new StormchaserDrake();
        StormchaserDrake secondDraw = new StormchaserDrake();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new MassiveMight(), new MassiveMight()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, drake.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, drake.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Only the targeted Drake triggers when multiple Drakes are controlled")
    void onlyTargetedDrakeDraws() {
        Permanent drake = addCreatureReady(player1, new StormchaserDrake());
        addCreatureReady(player1, new StormchaserDrake());
        StormchaserDrake drawnCard = new StormchaserDrake();
        harness.setLibrary(player1, List.of(drawnCard, new StormchaserDrake()));
        harness.setHand(player1, List.of(new MassiveMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, drake.getId());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
