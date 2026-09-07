package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NullGroupBiologicalAssets.class, GrizzlyBears.class})
class NullGroupBiologicalAssetsTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn only")
    void hasFirstStrikeDuringControllerTurnOnly() {
        Permanent creature = addCreatureReady(player1, new NullGroupBiologicalAssets());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("May discard a card to draw a card when it attacks")
    void mayDiscardToDrawWhenAttacking() {
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addCreatureReady(player1, new NullGroupBiologicalAssets());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining the attack trigger does not discard or draw")
    void decliningAttackTriggerDoesNothing() {
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new NullGroupBiologicalAssets());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
