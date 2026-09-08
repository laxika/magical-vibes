package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolatileWanderglyph.class, Forest.class, GrizzlyBears.class})
class VolatileWanderglyphTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped lets you discard a card and draw a card")
    void becomingTappedCanDiscardAndDraw() {
        Permanent wanderglyph = harness.addToBattlefieldAndReturn(player1, new VolatileWanderglyph());
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(drawn));

        tapAndResolve(wanderglyph);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining the tapped ability does not discard or draw")
    void decliningTappedAbilityDoesNothing() {
        Permanent wanderglyph = harness.addToBattlefieldAndReturn(player1, new VolatileWanderglyph());
        Card kept = new GrizzlyBears();
        Card topCard = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(kept)));
        harness.setLibrary(player1, List.of(topCard));

        tapAndResolve(wanderglyph);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Volatile Wanderglyph")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new VolatileWanderglyph());
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
