package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArmoryMice;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.d.DesperateParry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObyraDreamingDuelist.class, ObyrasAttendants.class, DesperateParry.class, ArmoryMice.class,
        Bitterblossom.class})
class ObyraDreamingDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when another Faerie enters under your control")
    void drainsEachOpponentForAnotherFaerie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.enterBattlefieldAndReturn(player1, new ObyraDreamingDuelist());

        harness.enterBattlefieldAndReturn(player1, new ObyrasAttendants());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger for a non-Faerie or an opponent's Faerie")
    void doesNotTriggerForNonFaerieOrOpponentFaerie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.enterBattlefieldAndReturn(player1, new ObyraDreamingDuelist());

        harness.enterBattlefieldAndReturn(player1, new ArmoryMice());
        harness.enterBattlefieldAndReturn(player2, new ObyrasAttendants());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Obyra does not trigger for its own entry")
    void doesNotTriggerForItsOwnEntry() {
        harness.enterBattlefieldAndReturn(player1, new ObyraDreamingDuelist());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Faerie entry creates a separate life-loss trigger")
    void triggersForEveryFaerieEntry() {
        harness.enterBattlefieldAndReturn(player1, new ObyraDreamingDuelist());
        harness.enterBattlefieldAndReturn(player1, new ObyrasAttendants());
        harness.enterBattlefieldAndReturn(player1, new ObyrasAttendants());

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A noncreature Faerie entering also triggers Obyra")
    void triggersForKindredFaerieEnchantment() {
        harness.enterBattlefieldAndReturn(player1, new ObyraDreamingDuelist());

        harness.castFromHand(player1, new Bitterblossom(), "{1}{B}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bitterblossom");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Obyra can be cast during the opponent's upkeep")
    void canBeCastAtInstantSpeed() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new ObyraDreamingDuelist(), "{U}{B}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Obyra, Dreaming Duelist");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Obyra cannot be blocked by a creature without flying or reach")
    void flyingRestrictsBlockers() {
        var obyra = harness.addToBattlefieldAndReturn(player1, new ObyraDreamingDuelist());
        var groundBlocker = harness.addToBattlefieldAndReturn(player2, new ArmoryMice());
        var flyingBlocker = harness.addToBattlefieldAndReturn(player2, new ObyrasAttendants());

        assertThat(bls.canBlockAttacker(gd, groundBlocker, obyra,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, obyra,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
