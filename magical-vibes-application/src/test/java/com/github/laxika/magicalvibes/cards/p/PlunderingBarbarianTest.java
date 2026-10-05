package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SpareDagger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlunderingBarbarian.class, SpareDagger.class})
class PlunderingBarbarianTest extends BaseCardTest {

    @Test
    void smashTheChestDestroysTargetArtifact() {
        Permanent dagger = harness.addToBattlefieldAndReturn(player2, new SpareDagger());
        castBarbarian();

        harness.handleListChoice(player1, "Smash the Chest \u2014 Destroy target artifact.");
        harness.handlePermanentChosen(player1, dagger.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spare Dagger");
    }

    @Test
    void pryItOpenCreatesTreasureToken() {
        castBarbarian();

        harness.handleListChoice(player1, "Pry It Open \u2014 Create a Treasure token.");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void smashTheChestCanDestroyYourOwnArtifact() {
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new SpareDagger());
        castBarbarian();

        harness.handleListChoice(player1, "Smash the Chest \u2014 Destroy target artifact.");
        harness.handlePermanentChosen(player1, dagger.getId());
        harness.assertOnBattlefield(player1, "Spare Dagger");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spare Dagger");
        harness.assertInGraveyard(player1, "Spare Dagger");
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    void pryItOpenCanBeChosenWhenAnArtifactIsAvailable() {
        harness.addToBattlefield(player2, new SpareDagger());
        castBarbarian();

        harness.handleListChoice(player1, "Pry It Open \u2014 Create a Treasure token.");
        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Treasure");
        harness.assertOnBattlefield(player2, "Spare Dagger");
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    private void castBarbarian() {
        harness.setHand(player1, List.of(new PlunderingBarbarian()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
