package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViashinoLashclaw.class, GrizzlyBears.class})
class ViashinoLashclawTest extends BaseCardTest {

    @Test
    void discardingCardGrantsHasteToCreaturesYouControlUntilEndOfTurn() {
        Permanent lashclaw = addCreatureReady(player1, new ViashinoLashclaw());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(lashclaw.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, lashclaw, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lashclaw, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new ViashinoLashclaw());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }
}
