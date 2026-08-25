package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidInkshrouder.class, GrizzlyBears.class})
class CephalidInkshrouderTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card grants shroud and makes this creature unblockable")
    void discardGrantsShroudAndUnblockable() {
        Permanent inkshrouder = harness.addToBattlefieldAndReturn(player1, new CephalidInkshrouder());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, inkshrouder, Keyword.SHROUD)).isTrue();
        assertThat(inkshrouder.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Shroud and unblockability wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent inkshrouder = harness.addToBattlefieldAndReturn(player1, new CephalidInkshrouder());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, inkshrouder, Keyword.SHROUD)).isFalse();
        assertThat(inkshrouder.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefieldAndReturn(player1, new CephalidInkshrouder());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
