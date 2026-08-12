package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BalothWoodcrasherTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Baloth Woodcrasher +4/+4 and trample until end of turn")
    void landfallBoostsAndGrantsTrample() {
        Permanent baloth = harness.addToBattlefieldAndReturn(player1, new BalothWoodcrasher());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(baloth.getEffectivePower()).isEqualTo(8);
        assertThat(baloth.getEffectiveToughness()).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, baloth, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Landfall boost and trample wear off at end of turn")
    void landfallEffectWearsOff() {
        Permanent baloth = harness.addToBattlefieldAndReturn(player1, new BalothWoodcrasher());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(baloth.getEffectivePower()).isEqualTo(4);
        assertThat(baloth.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, baloth, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's land does not trigger Baloth Woodcrasher")
    void opponentLandDoesNotTrigger() {
        Permanent baloth = harness.addToBattlefieldAndReturn(player1, new BalothWoodcrasher());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(baloth.getEffectivePower()).isEqualTo(4);
        assertThat(baloth.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, baloth, Keyword.TRAMPLE)).isFalse();
    }
}
