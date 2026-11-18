package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanopyBaloth.class, Forest.class})
class CanopyBalothTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Canopy Baloth +2/+2 until end of turn")
    void landfallBoostsCanopyBaloth() {
        Permanent baloth = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(5);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent baloth = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Canopy Baloth")
    void opponentLandDoesNotTrigger() {
        Permanent baloth = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lands entering without being played trigger each Baloth and the boosts accumulate")
    void landEntriesBoostEachBalothAndAccumulate() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(7);
    }
}
