package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WormfangTurtle.class, KrosanVerge.class})
class WormfangTurtleTest extends BaseCardTest {

    @Test
    void exilesOneLandYouControlAndReturnsItWhenTurtleLeaves() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        harness.castFromHand(player1, new WormfangTurtle(), "{2}{U}");
        resolveAllTriggers();

        Permanent turtle = findPermanent(player1, "Wormfang Turtle");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId())
                .doesNotContain(opponentLand.getId(), turtle.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, secondLand.getId());

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(secondLand.getCard().getId()));
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(firstLand.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondLand.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentLand);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, turtle));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(secondLand.getCard().getId()));
        assertThat(gd.exiledCards)
                .noneMatch(entry -> entry.card().getId().equals(secondLand.getCard().getId()));
    }

    @Test
    void doesNothingWhenYouControlNoLand() {
        harness.castFromHand(player1, new WormfangTurtle(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void returnsExiledLandToItsOwnerWhenTurtleLeaves() {
        KrosanVerge landCard = new KrosanVerge();
        landCard.setOwnerId(player2.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player1, landCard);

        harness.castFromHand(player1, new WormfangTurtle(), "{2}{U}");
        resolveAllTriggers();

        Permanent turtle = findPermanent(player1, "Wormfang Turtle");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.getCardsExiledByPermanent(turtle.getId())).containsExactly(landCard);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, turtle));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(landCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(landCard.getId()));
        assertThat(gd.exiledCards)
                .noneMatch(entry -> entry.card().getId().equals(landCard.getId()));
    }

    @Test
    void landExiledAfterTurtleLeavesBeforeEnterTriggerResolvesStaysExiled() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());

        harness.castFromHand(player1, new WormfangTurtle(), "{2}{U}");
        harness.passBothPriorities();

        Permanent turtle = findPermanent(player1, "Wormfang Turtle");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, turtle));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(land.getCard().getId())
                        && turtle.getId().equals(entry.sourcePermanentId()));
    }
}
