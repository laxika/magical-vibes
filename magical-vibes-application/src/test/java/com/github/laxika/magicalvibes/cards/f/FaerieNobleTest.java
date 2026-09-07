package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AnabaBodyguard;
import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieNoble.class, WillowFaerie.class, AnabaBodyguard.class})
class FaerieNobleTest extends BaseCardTest {

    @Test
    @DisplayName("Other Faerie creatures you control get +0/+1")
    void staticBuffsOtherFaeries() {
        harness.addToBattlefield(player1, new FaerieNoble());
        harness.addToBattlefield(player1, new WillowFaerie());

        Permanent faerie = findPermanent(player1, "Willow Faerie");

        assertThat(gqs.getEffectivePower(gd, faerie)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, faerie)).isEqualTo(3);
    }

    @Test
    @DisplayName("Faerie Noble does not buff itself or non-Faeries")
    void staticExcludesSelfAndNonFaeries() {
        harness.addToBattlefield(player1, new FaerieNoble());
        harness.addToBattlefield(player1, new AnabaBodyguard());

        Permanent noble = findPermanent(player1, "Faerie Noble");
        Permanent bodyguard = findPermanent(player1, "Anaba Bodyguard");

        assertThat(gqs.getEffectiveToughness(gd, noble)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bodyguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Static buff does not apply to opponent's Faeries")
    void staticExcludesOpponentFaeries() {
        harness.addToBattlefield(player1, new FaerieNoble());
        harness.addToBattlefield(player2, new WillowFaerie());

        Permanent faerie = findPermanent(player2, "Willow Faerie");

        assertThat(gqs.getEffectiveToughness(gd, faerie)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T} ability gives other Faeries you control +1/+0 until end of turn")
    void tapAbilityBuffsOtherFaeries() {
        Permanent noble = addCreatureReady(player1, new FaerieNoble());
        addCreatureReady(player1, new WillowFaerie());
        addCreatureReady(player1, new AnabaBodyguard());

        harness.activateAbility(player1, 0, null, null);
        assertThat(noble.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent faerie = findPermanent(player1, "Willow Faerie");
        Permanent bodyguard = findPermanent(player1, "Anaba Bodyguard");

        assertThat(gqs.getEffectivePower(gd, faerie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, faerie)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, noble)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bodyguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T} ability boost wears off at end of turn")
    void tapAbilityBoostWearsOff() {
        addCreatureReady(player1, new FaerieNoble());
        addCreatureReady(player1, new WillowFaerie());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent faerie = findPermanent(player1, "Willow Faerie");
        assertThat(gqs.getEffectivePower(gd, faerie)).isEqualTo(2);

        advanceToNextTurn();

        assertThat(gqs.getEffectivePower(gd, faerie)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, faerie)).isEqualTo(3);
    }

    @Test
    @DisplayName("{T} ability does not boost an opponent's Faeries")
    void tapAbilityExcludesOpponentFaeries() {
        addCreatureReady(player1, new FaerieNoble());
        Permanent opponentFaerie = addCreatureReady(player2, new WillowFaerie());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentFaerie)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentFaerie)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T} ability boosts another Faerie Noble but not the activating one")
    void tapAbilityExcludesOnlyActivatingNoble() {
        Permanent activatingNoble = addCreatureReady(player1, new FaerieNoble());
        Permanent otherNoble = addCreatureReady(player1, new FaerieNoble());
        Permanent faerie = addCreatureReady(player1, new WillowFaerie());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, activatingNoble)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherNoble)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, faerie)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T} ability does not affect Faeries entering later that turn")
    void tapAbilityDoesNotAffectLaterFaeries() {
        addCreatureReady(player1, new FaerieNoble());
        addCreatureReady(player1, new WillowFaerie());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent laterFaerie = addCreatureReady(player1, new WillowFaerie());

        assertThat(gqs.getEffectivePower(gd, laterFaerie)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterFaerie)).isEqualTo(3);
    }

    private void advanceToNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.UPKEEP);
    }
}
