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

    @Test
    @DisplayName("A land drawn by the spell can be chosen, and only one land enters")
    void canChooseNewlyDrawnLand() {
        Forest first = new Forest();
        Forest second = new Forest();
        LessonsFromLife otherCard = new LessonsFromLife();
        harness.setLibrary(player1, List.of(first, second, otherCard));
        harness.castFromHand(player1, new LessonsFromLife(), "{2}{G}{U}");

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, otherCard);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Forest").getCard()).isSameAs(second);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, otherCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Drawing still completes when no land is available")
    void resolvesWithoutLandInHand() {
        LessonsFromLife first = new LessonsFromLife();
        LessonsFromLife second = new LessonsFromLife();
        LessonsFromLife third = new LessonsFromLife();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new LessonsFromLife(), "{2}{G}{U}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }
}
