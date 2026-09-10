package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CemeteryGatekeeper.class, Forest.class, GrizzlyBears.class})
class CemeteryGatekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability exiles and remembers a graveyard card")
    void exilesAndImprintsChosenCard() {
        Card exiled = new GrizzlyBears();
        Permanent gatekeeper = enterGatekeeperWith(exiled);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiled);
        assertThat(gd.getImprintedCard(gatekeeper.getCard())).isSameAs(exiled);
    }

    @Test
    @DisplayName("A matching spell deals damage to the player who cast it")
    void matchingSpellDamagesCaster() {
        enterGatekeeperWith(new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castCreature(player2, 0);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("A matching land deals damage to the player who played it")
    void matchingLandDamagesPlayerWhoPlayedIt() {
        enterGatekeeperWith(new Forest());
        playLand(player2, new Forest());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A matching land played by its controller deals damage to that player")
    void matchingLandDamagesController() {
        enterGatekeeperWith(new Forest());
        playLand(player1, new Forest());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A land with no shared card type does not deal damage")
    void nonmatchingLandDoesNotDealDamage() {
        enterGatekeeperWith(new GrizzlyBears());
        playLand(player1, new Forest());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private Permanent enterGatekeeperWith(Card card) {
        harness.setGraveyard(player2, List.of(card));
        Permanent gatekeeper = harness.enterBattlefieldAndReturn(player1, new CemeteryGatekeeper());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        return gatekeeper;
    }

    private void playLand(com.github.laxika.magicalvibes.model.Player player, Card land) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(land));
        harness.playLand(player, 0);
        harness.passBothPriorities();
    }

    private void resolveStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
