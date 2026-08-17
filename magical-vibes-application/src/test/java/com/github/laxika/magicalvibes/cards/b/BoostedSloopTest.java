package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoostedSloopTest extends BaseCardTest {

    @Test
    void attacksDrawsThenDiscards() {
        attackWithSloop(List.of(new GrizzlyBears()), new Forest());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(card -> card.getName()).isEqualTo("Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void crewAnimatesVehicleAndResetsAtEndOfTurn() {
        Permanent sloop = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());
        sloop.setSummoningSick(false);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sloop)).isTrue();
        assertThat(creature.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sloop)).isFalse();
    }

    private Permanent attackWithSloop(List<Card> hand, Card cardToDraw) {
        Permanent sloop = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());
        sloop.setSummoningSick(false);
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(cardToDraw));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        return sloop;
    }
}
