package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, secondLand.getId());

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(secondLand.getCard().getId()));
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(firstLand.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondLand.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentLand);

        Permanent turtle = findPermanent(player1, "Wormfang Turtle");
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
    void returnsExiledLandToItsOwner() {
        KrosanVerge ownedByOpponent = new KrosanVerge();
        ownedByOpponent.setOwnerId(player2.getId());
        Permanent stolenLand = harness.addToBattlefieldAndReturn(player1, ownedByOpponent);

        harness.castFromHand(player1, new WormfangTurtle(), "{2}{U}");
        resolveAllTriggers();

        Permanent turtle = findPermanent(player1, "Wormfang Turtle");
        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, turtle.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactly(stolenLand.getCard());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, turtle));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(stolenLand.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(stolenLand.getCard().getId()));
    }

    @Test
    void leavesLandExiledWhenTurtleLeavesBeforeEnterTriggerResolves() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());

        harness.castFromHand(player1, new WormfangTurtle(), "{2}{U}");
        harness.passBothPriorities();

        Permanent turtle = findPermanent(player1, "Wormfang Turtle");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, turtle));
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());

        harness.handlePermanentChosen(player1, secondLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondLand.getId()));
        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, turtle.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactly(secondLand.getCard());
    }
}
