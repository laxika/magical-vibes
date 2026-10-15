package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KazanduStomper.class, Forest.class})
class KazanduStomperTest extends BaseCardTest {

    @Test
    void returnsUpToTwoLandsControlledByItsController() {
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent thirdForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        castStomper();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(firstForest.getId(), secondForest.getId(), thirdForest.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstForest.getId(), secondForest.getId()));

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Forest"))).hasSize(2);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Kazandu Stomper");
    }

    @Test
    void mayReturnNoLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castStomper();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Forest"))).isEmpty();
        assertThat(findPermanents(player1, "Forest")).hasSize(2);
    }

    @Test
    void canReturnOneLandWhenOnlyOneIsControlled() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castStomper();

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Kazandu Stomper");
    }

    @Test
    void resolvesWithoutAChoiceWhenNoLandsAreControlled() {
        harness.addToBattlefield(player2, new Forest());
        castStomper();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Kazandu Stomper");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    void canReturnOnlyOneLandWhenSeveralAreControlled() {
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castStomper();

        harness.handleMultiplePermanentsChosen(player1, List.of(firstForest.getId()));

        assertThat(findPermanents(player1, "Forest")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void returnsAControlledLandToItsOwnersHand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.stolenCreatures.put(forest.getId(), player2.getId());
        castStomper();

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertOnBattlefield(player1, "Kazandu Stomper");
    }

    private void castStomper() {
        harness.castFromHand(player1, new KazanduStomper(), "{5}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
