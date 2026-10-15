package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlandraSkyDreamer.class, MaskwoodNexus.class})
class AlandraSkyDreamerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying Drake on the second draw and boosts Alandra and Drakes on the fifth")
    void createsAndBoostsOnDrawThresholds() {
        Permanent alandra = harness.addToBattlefieldAndReturn(player1, new AlandraSkyDreamer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlandraSkyDreamer(), new AlandraSkyDreamer(),
                new AlandraSkyDreamer(), new AlandraSkyDreamer(), new AlandraSkyDreamer(), new AlandraSkyDreamer()));

        draw(player1);
        draw(player1);
        assertThat(findPermanents(player1, "Drake")).isEmpty();

        harness.passBothPriorities();

        List<Permanent> drakes = findPermanents(player1, "Drake");
        assertThat(drakes).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, drakes.get(0))).isEqualTo(2);

        draw(player1);
        draw(player1);
        draw(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, alandra)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, drakes.get(0))).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, drakes.get(0))).isEqualTo(7);

        draw(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, drakes.get(0))).isEqualTo(7);
    }

    @Test
    void locksHandSizeAtResolutionRatherThanAtFifthDraw() {
        Permanent alandra = harness.addToBattlefieldAndReturn(player1, new AlandraSkyDreamer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlandraSkyDreamer(), new AlandraSkyDreamer(),
                new AlandraSkyDreamer(), new AlandraSkyDreamer(), new AlandraSkyDreamer(),
                new AlandraSkyDreamer(), new AlandraSkyDreamer()));

        draw(player1);
        draw(player1);
        harness.passBothPriorities();
        Permanent drake = findPermanents(player1, "Drake").getFirst();
        assertThat(gqs.hasKeyword(gd, drake, Keyword.FLYING)).isTrue();
        draw(player1);
        draw(player1);
        draw(player1);
        draw(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, alandra)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(8);
        draw(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(8);
    }

    @Test
    void countsDrawsBeforeAlandraEnteredTheBattlefield() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlandraSkyDreamer(), new AlandraSkyDreamer(),
                new AlandraSkyDreamer(), new AlandraSkyDreamer(), new AlandraSkyDreamer()));
        draw(player1);
        harness.addToBattlefield(player1, new AlandraSkyDreamer());
        draw(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Drake")).hasSize(1);
    }

    @Test
    void doesNotTriggerForOpponentsDraws() {
        Permanent alandra = harness.addToBattlefieldAndReturn(player1, new AlandraSkyDreamer());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new AlandraSkyDreamer(), new AlandraSkyDreamer(),
                new AlandraSkyDreamer(), new AlandraSkyDreamer(), new AlandraSkyDreamer()));
        for (int i = 0; i < 5; i++) {
            draw(player2);
        }
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Drake")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(2);
    }

    @Test
    void boostsAlandraOnlyOnceWhenSheIsAlsoADrake() {
        Permanent alandra = harness.addToBattlefieldAndReturn(player1, new AlandraSkyDreamer());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlandraSkyDreamer(), new AlandraSkyDreamer(),
                new AlandraSkyDreamer(), new AlandraSkyDreamer(), new AlandraSkyDreamer()));
        draw(player1);
        draw(player1);
        harness.passBothPriorities();
        Permanent drake = findPermanents(player1, "Drake").getFirst();
        draw(player1);
        draw(player1);
        draw(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, alandra)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(7);
    }

    @Test
    void bonusesExpireAndDrawThresholdsResetOnOpponentsTurn() {
        Permanent alandra = harness.addToBattlefieldAndReturn(player1, new AlandraSkyDreamer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlandraSkyDreamer(), new AlandraSkyDreamer(),
                new AlandraSkyDreamer(), new AlandraSkyDreamer(), new AlandraSkyDreamer(),
                new AlandraSkyDreamer(), new AlandraSkyDreamer()));
        draw(player1);
        draw(player1);
        harness.passBothPriorities();
        Permanent drake = findPermanents(player1, "Drake").getFirst();
        draw(player1);
        draw(player1);
        draw(player1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, alandra)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
        draw(player1);
        assertThat(gd.stack).isEmpty();
        draw(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Drake")).hasSize(2);
    }

    @Test
    void fifthDrawCountsEarlierDrawsButDoesNotRecreateMissedSecondDrawTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlandraSkyDreamer(), new AlandraSkyDreamer(),
                new AlandraSkyDreamer(), new AlandraSkyDreamer(), new AlandraSkyDreamer()));
        for (int i = 0; i < 4; i++) {
            draw(player1);
        }
        Permanent alandra = harness.addToBattlefieldAndReturn(player1, new AlandraSkyDreamer());
        draw(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Drake")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, alandra)).isEqualTo(9);
    }

    @Test
    void enteringAfterFifthDrawDoesNotTriggerOnSixthDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlandraSkyDreamer(), new AlandraSkyDreamer(),
                new AlandraSkyDreamer(), new AlandraSkyDreamer(), new AlandraSkyDreamer(), new AlandraSkyDreamer()));
        for (int i = 0; i < 5; i++) {
            draw(player1);
        }
        Permanent alandra = harness.addToBattlefieldAndReturn(player1, new AlandraSkyDreamer());
        draw(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Drake")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(2);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
