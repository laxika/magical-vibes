package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CarrierThrall;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmSurge.class, CarrierThrall.class, SludgeCrawler.class})
class SwarmSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures, and gives first strike to your colorless creatures")
    void boostsOwnCreaturesAndGrantsFirstStrikeToColorlessCreatures() {
        Permanent thrall = harness.addToBattlefieldAndReturn(player1, new CarrierThrall());
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new SludgeCrawler());
        Permanent opponentThrall = harness.addToBattlefieldAndReturn(player2, new CarrierThrall());

        cast();

        assertThat(gqs.getEffectivePower(gd, thrall)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, thrall)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, thrall, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, crawler, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentThrall)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentThrall, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bonuses wear off at end of turn")
    void bonusesWearOffAtEndOfTurn() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new SludgeCrawler());

        cast();
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, crawler, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, crawler, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opposing colorless creatures receive neither bonus")
    void opposingColorlessCreaturesAreUnaffected() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player2, new SludgeCrawler());

        cast();

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, crawler, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution receive neither bonus")
    void creaturesEnteringAfterResolutionAreUnaffected() {
        cast();

        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new SludgeCrawler());
        Permanent thrall = harness.addToBattlefieldAndReturn(player1, new CarrierThrall());

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, crawler, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, thrall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, thrall)).isEqualTo(1);
    }

    private void cast() {
        harness.setHand(player1, List.of(new SwarmSurge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
