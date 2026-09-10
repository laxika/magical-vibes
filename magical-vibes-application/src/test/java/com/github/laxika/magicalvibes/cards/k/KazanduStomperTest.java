package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    private void castStomper() {
        harness.setHand(player1, List.of(new KazanduStomper()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
