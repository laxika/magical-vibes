package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OniOfWildPlacesTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new OniOfWildPlaces());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Offers only red creatures controlled by its controller")
    void offersOnlyRedCreaturesControlledByController() {
        Permanent oni = addCreatureReady(player1, new OniOfWildPlaces());
        Permanent redCreature = addCreatureReady(player1, new RagingGoblin());
        Permanent nonRedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentRedCreature = addCreatureReady(player2, new RagingGoblin());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds())
                .contains(oni.getId(), redCreature.getId())
                .doesNotContain(nonRedCreature.getId(), opponentRedCreature.getId());
    }

    @Test
    @DisplayName("Returns the chosen red creature to its owner's hand")
    void returnsChosenRedCreatureToOwnersHand() {
        addCreatureReady(player1, new OniOfWildPlaces());
        Permanent redCreature = addCreatureReady(player1, new RagingGoblin());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, redCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(redCreature.getId()));
        harness.assertInHand(player1, "Raging Goblin");
    }
}
