package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ResoluteBlademaster.class, GrizzlyBears.class})
class ResoluteBlademasterTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives your creatures double strike")
    void ownAllyEntryGrantsDoubleStrike() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ResoluteBlademaster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blademaster = findPermanent(player1, "Resolute Blademaster");
        assertThat(gqs.hasKeyword(gd, blademaster, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry gives your creatures double strike")
    void anotherAllyEntryGrantsDoubleStrike() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new ResoluteBlademaster());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ResoluteBlademaster()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent entering = findPermanents(player1, "Resolute Blademaster").get(1);
        assertThat(gqs.hasKeyword(gd, blademaster, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, entering, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new ResoluteBlademaster());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blademaster, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Granted double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new ResoluteBlademaster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blademaster = findPermanent(player1, "Resolute Blademaster");
        assertThat(gqs.hasKeyword(gd, blademaster, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blademaster, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
