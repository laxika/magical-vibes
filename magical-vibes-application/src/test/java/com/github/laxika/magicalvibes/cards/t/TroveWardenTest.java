package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TroveWarden.class, Forest.class, GrizzlyBears.class, ColossalDreadmaw.class, GiantGrowth.class})
class TroveWardenTest extends BaseCardTest {

    @Test
    void landfallExilesMatchingPermanentCardAndDeathReturnsIt() {
        Card validCard = new GrizzlyBears();
        Card expensivePermanent = new ColossalDreadmaw();
        Card nonPermanent = new GiantGrowth();
        harness.setGraveyard(player1, List.of(validCard, expensivePermanent, nonPermanent));
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new TroveWarden());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(validCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(validCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(warden.getId())).containsExactly(validCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(expensivePermanent, nonPermanent);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, warden);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(warden.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Trove Warden");
    }

    @Test
    void landfallDoesNotTriggerForOpponentsLand() {
        Card validCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(validCard));
        harness.addToBattlefield(player1, new TroveWarden());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(validCard);
    }

    @Test
    void landfallOnlyTargetsControllerGraveyard() {
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addToBattlefield(player1, new TroveWarden());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }
}
