package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunsBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Sun's Bounty gains 4 life when it resolves")
    void gainsLife() {
        harness.setHand(player1, List.of(new SunsBounty()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Sun's Bounty");
    }

    @Test
    @DisplayName("Recover returns Sun's Bounty to its owner's hand when paid")
    void recoverReturnsSourceToHand() {
        Card bounty = new SunsBounty();
        harness.setGraveyard(player1, List.of(bounty));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bounty);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bounty);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bounty);
    }
}
