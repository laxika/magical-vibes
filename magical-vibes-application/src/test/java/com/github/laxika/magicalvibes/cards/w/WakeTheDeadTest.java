package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WakeTheDead.class, GrizzlyBears.class, HillGiant.class})
class WakeTheDeadTest extends BaseCardTest {

    @Test
    void returnsExactlyXCreaturesAndSacrificesThemAtNextEndStep() {
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        harness.setGraveyard(player1, List.of(bears, giant));
        harness.setHand(player1, List.of(new WakeTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passPriority(player2);
        harness.castInstantForX(player1, 0, 2, List.of(bears.getId(), giant.getId()));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), giant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), giant.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void cannotBeCastOutsideAnOpponentsCombat() {
        harness.setHand(player1, List.of(new WakeTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
