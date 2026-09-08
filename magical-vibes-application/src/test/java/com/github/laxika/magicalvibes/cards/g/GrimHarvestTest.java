package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrimHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Grim Harvest returns a targeted creature card to its owner's hand")
    void returnsTargetedCreatureCard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new GrimHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        harness.assertInGraveyard(player1, "Grim Harvest");
    }

    @Test
    @DisplayName("Grim Harvest recover returns it to its owner's hand when paid")
    void recoverReturnsSourceWhenPaid() {
        Card harvest = new GrimHarvest();
        harness.setGraveyard(player1, List.of(harvest));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(harvest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(harvest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(harvest);
    }

    @Test
    @DisplayName("Grim Harvest recover exiles it when declined")
    void recoverExilesSourceWhenDeclined() {
        Card harvest = new GrimHarvest();
        harness.setGraveyard(player1, List.of(harvest));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(harvest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(harvest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(harvest);
    }
}
