package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OliviaCrimsonBride.class, GrizzlyBears.class, HerosDownfall.class})
class OliviaCrimsonBrideTest extends BaseCardTest {

    @Test
    void returnsACreatureTappedAndAttacking() {
        addReadyOlivia();
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        resolveAttackTrigger();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesReturnedCreatureWhenNoLegendaryVampireIsControlled() {
        Permanent olivia = addReadyOlivia();
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        resolveAttackTrigger();

        harness.setHand(player1, List.of(new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, olivia.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId())
                .isEqualTo(findPermanent(player1, "Grizzly Bears").getId());

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(returnedCard.getId()));
    }

    private Permanent addReadyOlivia() {
        return addCreatureReady(player1, new OliviaCrimsonBride());
    }

    private void resolveAttackTrigger() {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
