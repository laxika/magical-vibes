package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarrenInstigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage offers a Goblin creature from hand")
    void dealsDamageAndOffersGoblinCreature() {
        harness.setHand(player1, List.of(new RagingGoblin()));
        attackAndResolveOneTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Only Goblin creature cards are offered")
    void offersOnlyGoblinCreatures() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new RagingGoblin()));
        attackAndResolveOneTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Declining leaves the Goblin creature in hand")
    void decliningLeavesGoblinInHand() {
        harness.setHand(player1, List.of(new RagingGoblin()));
        attackAndResolveOneTrigger();

        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Raging Goblin");
        harness.assertNotOnBattlefield(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("No Goblin creature in hand does not prompt")
    void noGoblinCreatureDoesNotPrompt() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        attackAndResolveOneTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void attackAndResolveOneTrigger() {
        addCreatureReady(player1, new WarrenInstigator());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
    }
}
