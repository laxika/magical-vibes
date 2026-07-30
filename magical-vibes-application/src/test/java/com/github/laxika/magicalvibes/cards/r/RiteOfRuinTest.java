package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiteOfRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts-creatures-lands order sacrifices 1 artifact, 2 creatures and 3 lands")
    void sacrificesOneTwoThreeInChosenOrder() {
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lands-creatures-artifacts order sacrifices only one land, sparing the other two")
    void chosenOrderChangesHowManyOfEachTypeAreLost() {
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent chosenForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenForest.getId()));

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Forest", "Forest");
    }

    @Test
    @DisplayName("Every player sacrifices, not just the controller")
    void bothPlayersSacrifice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new PithingNeedle());
        harness.setHand(player1, List.of(new RiteOfRuin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Pithing Needle");
    }
}
