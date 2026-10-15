package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamtailHeron.class, AlmightyBrushwagg.class})
class DreamtailHeronTest extends BaseCardTest {

    @BeforeEach
    void clearHands() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    @Test
    @DisplayName("Mutating draws a card")
    void mutatingDrawsACard() {
        Permanent heron = addCreatureReady(player1, new DreamtailHeron());
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, heron, List.of(heron.getCard()), player1.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Almighty Brushwagg");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting normally does not draw a card")
    void castingNormallyDoesNotDraw() {
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.castFromHand(player1, new DreamtailHeron(), "{4}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dreamtail Heron");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each mutation draws another card")
    void repeatedMutationsDrawOneCardEach() {
        Permanent heron = addCreatureReady(player1, new DreamtailHeron());
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg(), new AlmightyBrushwagg()));

        for (int i = 1; i <= 2; i++) {
            harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                    gd, heron, List.of(heron.getCard()), player1.getId()));
            resolveAllTriggers();
            assertThat(gd.playerHands.get(player1.getId())).hasSize(i);
        }
    }

    @Test
    @DisplayName("The mutation trigger draws for its controller even after the source leaves")
    void opponentControlledTriggerSurvivesSourceLeaving() {
        Permanent heron = addCreatureReady(player2, new DreamtailHeron());
        harness.setLibrary(player2, List.of(new AlmightyBrushwagg()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, heron, List.of(heron.getCard()), player2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(heron);
        gd.playerGraveyards.get(player2.getId()).add(heron.getCard());
        resolveAllTriggers();

        harness.assertInHand(player2, "Almighty Brushwagg");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
