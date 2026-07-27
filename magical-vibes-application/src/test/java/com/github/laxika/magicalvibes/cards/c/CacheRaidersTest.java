package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CacheRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger presents a mandatory permanent choice")
    void upkeepTriggerPresentsChoice() {
        addCreatureReady(player1, new CacheRaiders());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Chosen controlled creature is returned to its owner's hand")
    void returnsChosenControlledCreatureToHand() {
        addCreatureReady(player1, new CacheRaiders());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be forced to return itself when it is the only permanent")
    void returnsItselfWhenOnlyPermanent() {
        Permanent raiders = addCreatureReady(player1, new CacheRaiders());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, raiders.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cache Raiders");
        harness.assertInHand(player1, "Cache Raiders");
    }

    @Test
    @DisplayName("Only permanents the controller controls are legal choices")
    void onlyControlledPermanentsAreLegal() {
        Permanent raiders = addCreatureReady(player1, new CacheRaiders());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds())
                .contains(raiders.getId())
                .doesNotContain(opponentBears.getId());
    }
}
