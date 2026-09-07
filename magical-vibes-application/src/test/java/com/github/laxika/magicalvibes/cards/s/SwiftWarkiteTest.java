package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwiftWarkite.class, GrizzlyBears.class, HillGiant.class})
class SwiftWarkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an eligible creature from hand onto the battlefield with haste and returns it at the next end step")
    void putsCreatureFromHandWithHasteAndReturnsAtNextEndStep() {
        Card warkite = new SwiftWarkite();
        Card bear = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        harness.setHand(player1, List.of(warkite, bear, hillGiant));
        castWarkite();

        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(
                        entered.getId(), DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Swift Warkite");
    }

    @Test
    @DisplayName("Puts an eligible creature from the graveyard onto the battlefield")
    void putsCreatureFromGraveyardOntoBattlefield() {
        Card warkite = new SwiftWarkite();
        Card bear = new GrizzlyBears();
        harness.setHand(player1, List.of(warkite));
        harness.setGraveyard(player1, List.of(bear));
        castWarkite();

        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("May decline putting a creature onto the battlefield")
    void mayDeclinePuttingCreatureOntoBattlefield() {
        Card warkite = new SwiftWarkite();
        Card bear = new GrizzlyBears();
        harness.setHand(player1, List.of(warkite, bear));
        castWarkite();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bear.getId()));
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    private void castWarkite() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
