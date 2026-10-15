package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YaroksWavecrasher.class, GreenwoodSentinel.class, Island.class})
class YaroksWavecrasherTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts to return another creature you control")
    void etbPromptsBounceOfAnotherCreature() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.castFromHand(player1, new YaroksWavecrasher(), "{3}{U}");
        UUID sentinelId = harness.getPermanentId(player1, "Greenwood Sentinel");

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(sentinelId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen creature returns to its owner's hand")
    void chosenCreatureReturnsToHand() {
        addCreatureReady(player1, new GreenwoodSentinel());
        harness.castFromHand(player1, new YaroksWavecrasher(), "{3}{U}");
        UUID sentinelId = harness.getPermanentId(player1, "Greenwood Sentinel");

        resolveAllTriggers();
        harness.handlePermanentChosen(player1, sentinelId);

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInHand(player1, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Yarok's Wavecrasher");
    }

    @Test
    @DisplayName("The source and noncreatures are not valid choices")
    void sourceAndNoncreaturesExcluded() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new YaroksWavecrasher(), "{3}{U}");

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Yarok's Wavecrasher");
    }
    @Test
    @DisplayName("Opposing creatures cannot be returned even when no other friendly creature exists")
    void opposingCreatureIsNotAChoice() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.castFromHand(player1, new YaroksWavecrasher(), "{3}{U}");

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Yarok's Wavecrasher");
    }

    @Test
    @DisplayName("A creature controlled by you but owned by an opponent returns to that opponent's hand")
    void returnsCreatureToOwnerRatherThanController() {
        GreenwoodSentinel sentinel = new GreenwoodSentinel();
        sentinel.setOwnerId(player2.getId());
        UUID sentinelId = harness.addToBattlefieldAndReturn(player1, sentinel).getId();
        gd.stolenCreatures.put(sentinelId, player2.getId());
        harness.castFromHand(player1, new YaroksWavecrasher(), "{3}{U}");

        resolveAllTriggers();
        harness.handlePermanentChosen(player1, sentinelId);

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInHand(player2, "Greenwood Sentinel");
        harness.assertNotInHand(player1, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Yarok's Wavecrasher");
    }

    @Test
    @DisplayName("Another Wavecrasher is a valid choice alongside other friendly creatures")
    void canChooseAnotherWavecrasher() {
        UUID previousId = harness.addToBattlefieldAndReturn(player1, new YaroksWavecrasher()).getId();
        UUID sentinelId = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel()).getId();
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new YaroksWavecrasher(), "{3}{U}");

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(previousId, sentinelId);
        harness.handlePermanentChosen(player1, previousId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(previousId));
        harness.assertInHand(player1, "Yarok's Wavecrasher");
        harness.assertOnBattlefield(player1, "Yarok's Wavecrasher");
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
    }
}
