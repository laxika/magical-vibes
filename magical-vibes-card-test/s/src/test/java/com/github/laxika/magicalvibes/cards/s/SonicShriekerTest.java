package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SonicShrieker.class, GrizzlyBears.class})
class SonicShriekerTest extends BaseCardTest {

    @Test
    void damagesTargetPlayerGainsLifeAndMakesThatPlayerDiscard() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SonicShrieker(), "{2}{R}{W}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void damagesTargetCreatureAndGainsLifeWithoutDiscardingItsController() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SonicShrieker(), "{2}{R}{W}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void targetingYourselfGainsLifeAndDiscardsYourCard() {
        harness.castFromHand(player1, new SonicShrieker(), "{2}{R}{W}{B}");
        harness.setHand(player1, List.of(new SonicShrieker()));
        harness.setHand(player2, List.of(new SonicShrieker()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sonic Shrieker");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void damagedPlayerWithEmptyHandDoesNotNeedToChooseACard() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new SonicShrieker(), "{2}{R}{W}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
