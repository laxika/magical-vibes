package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AureliaTheLawAbove.class, Forest.class, GrizzlyBears.class})
class AureliaTheLawAboveTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when any player attacks with three creatures")
    void drawsAtThreeAttackers() {
        addReadyAurelia(player1);
        addReadyAttackers(player1, 3);
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Deals damage to opponents and gains life when any player attacks with five creatures")
    void dealsDamageAndGainsLifeAtFiveAttackers() {
        addReadyAurelia(player1);
        addReadyAttackers(player1, 5);
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(player1, List.of(1, 2, 3, 4, 5));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Triggers when an opponent attacks with five creatures")
    void triggersForOpponentAttackers() {
        addReadyAurelia(player1);
        addReadyAttackers(player2, 5);
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(player2, List.of(0, 1, 2, 3, 4));
        resolveAllTriggers();
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Two attackers do not trigger either ability")
    void doesNotTriggerBelowThreeAttackers() {
        addReadyAurelia(player1);
        addReadyAttackers(player1, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1, 2)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Four attackers trigger only the card draw ability")
    void fourAttackersOnlyDraw() {
        addReadyAurelia(player1);
        addReadyAttackers(player1, 4);
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(1, 2, 3, 4));
            assertThat(gd.stack).hasSize(1);
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Five attackers put two independent abilities on the stack")
    void fiveAttackersCreateSeparateTriggers() {
        addReadyAurelia(player1);
        addReadyAttackers(player1, 5);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1, 2, 3, 4, 5)));

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Aurelia counts toward the threshold and six attackers still draw only one card")
    void aureliaCountsAndTriggersOnlyOnceAboveThreshold() {
        addReadyAurelia(player1);
        addReadyAttackers(player1, 5);
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest, new Forest()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1, 2, 3, 4, 5));
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    private Permanent addReadyAurelia(Player player) {
        return addCreatureReady(player, new AureliaTheLawAbove());
    }

    private void addReadyAttackers(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new GrizzlyBears());
        }
    }
}
