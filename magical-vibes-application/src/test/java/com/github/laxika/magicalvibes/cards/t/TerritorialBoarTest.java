package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TerritorialBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 and vigilance when a creature with power 4 or greater enters")
    void triggersForPowerFourCreature() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new TerritorialBoar());

        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gameData, boar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gameData, boar)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gameData, boar, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when a creature with power less than 4 enters")
    void doesNotTriggerForLowPowerCreature() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new TerritorialBoar());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gameData, boar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gameData, boar)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gameData, boar, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new TerritorialBoar());

        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gqs.getEffectivePower(gameData, boar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gameData, boar)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gameData, boar, Keyword.VIGILANCE)).isFalse();
    }
}
