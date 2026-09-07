package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarChariot.class, BalduvianBears.class, Forest.class})
class WarChariotTest extends BaseCardTest {

    @Test
    @DisplayName("Activating grants trample to the target and taps War Chariot")
    void grantsTrample() {
        Permanent chariot = addCreatureReady(player1, new WarChariot());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(chariot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOff() {
        addCreatureReady(player1, new WarChariot());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Activating can target an opponent's creature")
    void targetsOpponentCreature() {
        Permanent chariot = addCreatureReady(player1, new WarChariot());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(chariot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Targeting a non-creature is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new WarChariot());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
