package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NightsquadCommando;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheHowlingCommandos.class, GrizzlyBears.class, NightsquadCommando.class})
class TheHowlingCommandosTest extends BaseCardTest {

    @Test
    @DisplayName("Repeated activations stack without tapping the source")
    void repeatedActivationsStack() {
        Permanent commandos = addCreatureReady(player1, new TheHowlingCommandos());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commandos)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, commandos)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, commandos, Keyword.VIGILANCE)).isTrue();
        assertThat(commandos.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped and summoning-sick Commandos can activate the ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent commandos = harness.addToBattlefieldAndReturn(player1, new TheHowlingCommandos());
        commandos.setSummoningSick(true);
        commandos.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commandos)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, commandos)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, commandos, Keyword.VIGILANCE)).isTrue();
        assertThat(commandos.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only creatures present when the ability resolves receive its effects")
    void affectedCreaturesAreDeterminedAtResolution() {
        addCreatureReady(player1, new TheHowlingCommandos());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new NightsquadCommando());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new NightsquadCommando());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The ability boosts your creatures and gives your Soldiers vigilance")
    void boostsOwnCreaturesAndGrantsSoldiersVigilance() {
        Permanent commandos = addCreatureReady(player1, new TheHowlingCommandos());
        Permanent soldier = addCreatureReady(player1, new NightsquadCommando());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentSoldier = addCreatureReady(player2, new NightsquadCommando());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        int soldierPower = gqs.getEffectivePower(gd, soldier);
        int soldierToughness = gqs.getEffectiveToughness(gd, soldier);
        int opponentSoldierPower = gqs.getEffectivePower(gd, opponentSoldier);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commandos)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, commandos)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, commandos, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(soldierToughness + 1);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(opponentSoldierPower);
        assertThat(gqs.hasKeyword(gd, opponentSoldier, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost and vigilance last until end of turn")
    void abilityWearsOffAtEndOfTurn() {
        Permanent soldier = addCreatureReady(player1, new NightsquadCommando());
        addCreatureReady(player1, new TheHowlingCommandos());
        int soldierPower = gqs.getEffectivePower(gd, soldier);
        int soldierToughness = gqs.getEffectiveToughness(gd, soldier);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(soldierToughness + 1);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.VIGILANCE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierPower);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(soldierToughness);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.VIGILANCE)).isFalse();
    }
}
