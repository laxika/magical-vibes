package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LessonsFromLife.class, Forest.class})
class LessonsFromLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and puts a chosen land onto the battlefield tapped")
    void drawsAndPutsLandTapped() {
        harness.setHand(player1, List.of(new LessonsFromLife(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining the land drop leaves the land in hand")
    void decliningLeavesLandInHand() {
        harness.setHand(player1, List.of(new LessonsFromLife(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }
}
