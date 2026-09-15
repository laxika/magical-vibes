package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WormfangNewt.class, KrosanVerge.class})
class WormfangNewtTest extends BaseCardTest {

    @Test
    void exilesOneLandYouControlAndReturnsItWhenNewtLeaves() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        harness.castFromHand(player1, new WormfangNewt(), "{1}{U}");
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

        Permanent newt = findPermanent(player1, "Wormfang Newt");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, newt));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(secondLand.getCard().getId()));
        assertThat(gd.exiledCards)
                .noneMatch(entry -> entry.card().getId().equals(secondLand.getCard().getId()));
    }

    @Test
    void doesNothingWhenYouControlNoLand() {
        harness.castFromHand(player1, new WormfangNewt(), "{1}{U}");
        resolveAllTriggers();

        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void returnsExiledLandToItsOwner() {
        KrosanVerge ownedByOpponent = new KrosanVerge();
        ownedByOpponent.setOwnerId(player2.getId());
        Permanent stolenLand = harness.addToBattlefieldAndReturn(player1, ownedByOpponent);

        harness.castFromHand(player1, new WormfangNewt(), "{1}{U}");
        resolveAllTriggers();

        Permanent newt = findPermanent(player1, "Wormfang Newt");
        assertThat(gd.exiledCards).anyMatch(entry ->
                entry.sourcePermanentId().equals(newt.getId())
                        && entry.card().getId().equals(stolenLand.getCard().getId())
                        && entry.ownerId().equals(player2.getId()));

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, newt));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(stolenLand.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(stolenLand.getCard().getId()));
        assertThat(gd.exiledCards)
                .noneMatch(entry -> entry.card().getId().equals(stolenLand.getCard().getId()));
    }

    @Test
    void leavesLandExiledWhenNewtLeavesBeforeEnterTriggerResolves() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());

        harness.castFromHand(player1, new WormfangNewt(), "{1}{U}");
        harness.passBothPriorities();

        Permanent newt = findPermanent(player1, "Wormfang Newt");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, newt));
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
        harness.handlePermanentChosen(player1, secondLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondLand.getId()));
        assertThat(gd.exiledCards).anyMatch(entry ->
                entry.sourcePermanentId().equals(newt.getId())
                        && entry.card().getId().equals(secondLand.getCard().getId()));
    }
}
