package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecklessRacerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped lets you discard a card and draw a card")
    void becomingTappedCanDiscardAndDraw() {
        Permanent racer = harness.addToBattlefieldAndReturn(player1, new RecklessRacer());
        Card discarded = new GrizzlyBears();
        Card drawn = new Island();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(drawn));

        tapAndResolve(racer);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining the tapped ability does not discard or draw")
    void decliningTappedAbilityDoesNothing() {
        Permanent racer = harness.addToBattlefieldAndReturn(player1, new RecklessRacer());
        Card kept = new GrizzlyBears();
        Card topCard = new Island();
        harness.setHand(player1, new ArrayList<>(List.of(kept)));
        harness.setLibrary(player1, List.of(topCard));

        tapAndResolve(racer);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Reckless Racer")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new RecklessRacer());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        other.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, other));

        assertThat(gd.stack).isEmpty();
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
