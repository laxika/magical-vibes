package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.b.BorealShelf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunsBounty.class, BorealDruid.class, BorealShelf.class})
class SunsBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Sun's Bounty gains 4 life when it resolves")
    void gainsLife() {
        harness.setHand(player1, List.of(new SunsBounty()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Sun's Bounty");
    }

    @Test
    @DisplayName("Recover returns Sun's Bounty to its owner's hand when paid")
    void recoverReturnsSourceToHand() {
        Card bounty = new SunsBounty();
        harness.setGraveyard(player1, List.of(bounty));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bounty);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bounty);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bounty);
    }

    @Test
    @DisplayName("Recover exiles Sun's Bounty when declined")
    void recoverExilesSourceWhenDeclined() {
        Card bounty = new SunsBounty();
        harness.setGraveyard(player1, List.of(bounty));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bounty);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bounty);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bounty);
    }

    @Test
    @DisplayName("Recover does not trigger when an opponent's creature enters their graveyard")
    void recoverDoesNotTriggerForOpponentCreature() {
        Card bounty = new SunsBounty();
        harness.setGraveyard(player1, List.of(bounty));
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bounty);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bounty);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bounty);
    }

    @Test
    @DisplayName("Recover does not trigger when a noncreature enters its controller's graveyard")
    void recoverDoesNotTriggerForNoncreature() {
        Card bounty = new SunsBounty();
        harness.setGraveyard(player1, List.of(bounty));
        Permanent shelf = harness.addToBattlefieldAndReturn(player1, new BorealShelf());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, shelf));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bounty);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bounty);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bounty);
    }

    @Test
    @DisplayName("Recover exiles Sun's Bounty when the chosen payment cannot be made")
    void recoverExilesSourceWhenPaymentCannotBeMade() {
        Card bounty = new SunsBounty();
        harness.setGraveyard(player1, List.of(bounty));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bounty);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bounty);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bounty);
    }

    @Test
    @DisplayName("Recover does not trigger while Sun's Bounty is in hand")
    void recoverDoesNotTriggerFromHand() {
        Card bounty = new SunsBounty();
        harness.setHand(player1, List.of(bounty));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(bounty);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bounty);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("An older recover trigger cannot move Sun's Bounty after it is recovered and recast")
    void olderRecoverTriggerCannotMoveRecastCard(boolean payForOlderTrigger) {
        Card bounty = new SunsBounty();
        harness.setGraveyard(player1, List.of(bounty));
        Permanent firstDruid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        Permanent secondDruid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstDruid);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondDruid);
        });
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(bounty);

        harness.castAndResolveInstant(player1, gd.playerHands.get(player1.getId()).indexOf(bounty));
        harness.assertLife(player1, 24);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bounty);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, payForOlderTrigger);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bounty);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bounty);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bounty);
    }
}
