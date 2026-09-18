package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Chastise;
import com.github.laxika.magicalvibes.cards.l.LightningSurge;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Chastise.class, LightningSurge.class, MentalNote.class, TreacherousWerewolf.class})
class TreacherousWerewolfTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 with seven cards in its controller's graveyard")
    void getsThresholdBoost() {
        fillGraveyard(player1, 7);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get +2/+2 below threshold")
    void noThresholdBoostBelowSevenCards() {
        fillGraveyard(player1, 6);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentsGraveyardDoesNotEnableThreshold() {
        fillGraveyard(player2, 7);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold boost updates when the controller's graveyard changes")
    void thresholdBoostUpdatesWithGraveyardChanges() {
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(2);

        fillGraveyard(player1, 7);
        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(4);

        fillGraveyard(player1, 6);
        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold death ability makes its controller lose 4 life")
    void thresholdDeathAbilityLosesLife() {
        fillGraveyard(player1, 7);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());
        killWithChastise(werewolf);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Threshold death ability uses the graveyard count when the creature dies")
    void thresholdDeathAbilityUsesCurrentThreshold() {
        fillGraveyard(player1, 6);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());
        fillGraveyard(player1, 7);

        killWithChastise(werewolf);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Threshold death ability is lost when the graveyard drops below seven")
    void thresholdDeathAbilityTurnsOffBelowThreshold() {
        fillGraveyard(player1, 7);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());
        fillGraveyard(player1, 6);

        killWithChastise(werewolf);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Threshold death ability is absent below seven graveyard cards")
    void thresholdDeathAbilityIsAbsentBelowThreshold() {
        fillGraveyard(player1, 6);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());
        killWithChastise(werewolf);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void killWithChastise(Permanent target) {
        target.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Chastise()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Chastise());
        }
        harness.setGraveyard(player, cards);
    }

    @Test
    @DisplayName("Opponent's graveyard does not count for threshold")
    void opponentGraveyardDoesNotCount() {
        fillGraveyardForJudReview(player2, 7);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold boost and death ability turn off when the graveyard drops below seven cards")
    void thresholdEffectsTurnOffWhenGraveyardShrinks() {
        fillGraveyardForJudReview(player1, 7);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new TreacherousWerewolf());

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(4);

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(2);

        killWithLightningSurgeForJudReview(werewolf);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void killWithLightningSurgeForJudReview(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningSurge()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player2, 0, 0, target.getId());
    }

    private void fillGraveyardForJudReview(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new MentalNote());
        }
        harness.setGraveyard(player, cards);
    }
}
