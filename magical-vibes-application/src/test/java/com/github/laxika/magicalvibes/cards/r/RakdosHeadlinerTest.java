package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosHeadliner.class, GrizzlyBears.class})
class RakdosHeadlinerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card pays echo and keeps Rakdos Headliner")
    void discardingPaysEchoAndKeepsHeadliner() {
        castAndResolveHeadliner(true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Rakdos Headliner");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining echo sacrifices Rakdos Headliner")
    void decliningEchoSacrificesHeadliner() {
        castAndResolveHeadliner(true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Rakdos Headliner");
        harness.assertInGraveyard(player1, "Rakdos Headliner");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Empty hand causes Rakdos Headliner to be sacrificed without a prompt")
    void emptyHandSacrificesHeadlinerWithoutPrompt() {
        castAndResolveHeadliner(false);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Rakdos Headliner");
        harness.assertInGraveyard(player1, "Rakdos Headliner");
    }

    private void castAndResolveHeadliner(boolean includeDiscardCard) {
        harness.setHand(player1, includeDiscardCard
                ? List.of(new RakdosHeadliner(), new GrizzlyBears())
                : List.of(new RakdosHeadliner()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Rakdos Headliner");
    }
}
