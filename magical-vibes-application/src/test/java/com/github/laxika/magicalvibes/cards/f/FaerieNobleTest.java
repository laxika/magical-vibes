package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FaerieNobleTest extends BaseCardTest {

    @Test
    @DisplayName("Other Faerie creatures you control get +0/+1")
    void staticBuffsOtherFaeries() {
        harness.addToBattlefield(player1, new FaerieNoble());
        harness.addToBattlefield(player1, new CloudSprite());

        Permanent sprite = findPermanent(player1, "Cloud Sprite");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
    }

    @Test
    @DisplayName("Faerie Noble does not buff itself or non-Faeries")
    void staticExcludesSelfAndNonFaeries() {
        harness.addToBattlefield(player1, new FaerieNoble());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent noble = findPermanent(player1, "Faerie Noble");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectiveToughness(gd, noble)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Static buff does not apply to opponent's Faeries")
    void staticExcludesOpponentFaeries() {
        harness.addToBattlefield(player1, new FaerieNoble());
        harness.addToBattlefield(player2, new CloudSprite());

        Permanent sprite = findPermanent(player2, "Cloud Sprite");

        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(1);
    }

    @Test
    @DisplayName("{T} ability gives other Faeries you control +1/+0 until end of turn")
    void tapAbilityBuffsOtherFaeries() {
        harness.addToBattlefield(player1, new FaerieNoble());
        harness.addToBattlefield(player1, new CloudSprite());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Faerie Noble").setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sprite = findPermanent(player1, "Cloud Sprite");
        Permanent noble = findPermanent(player1, "Faerie Noble");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, noble)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T} ability boost wears off at end of turn")
    void tapAbilityBoostWearsOff() {
        harness.addToBattlefield(player1, new FaerieNoble());
        harness.addToBattlefield(player1, new CloudSprite());
        findPermanent(player1, "Faerie Noble").setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sprite = findPermanent(player1, "Cloud Sprite");
        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);

        advanceToNextTurn();

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
    }

    private void advanceToNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
