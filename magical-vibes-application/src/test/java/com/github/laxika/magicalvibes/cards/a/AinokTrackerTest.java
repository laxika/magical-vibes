package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AinokTracker.class})
class AinokTrackerTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent tracker = findPermanent(player1, "Ainok Tracker");
        assertThat(tracker.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);
        harness.turnFaceUp(player1, trackerIndex);
        harness.passBothPriorities();

        assertThat(tracker.isFaceDown()).isFalse();
    }

    @Test
    void morphCannotBePaidWithOnlyColorlessMana() {
        Permanent tracker = addCreatureReady(player1, new AinokTracker());
        tracker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(tracker.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceDownTrackersTradeInCombat() {
        Permanent attacker = addCreatureReady(player1, new AinokTracker());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent blocker = addCreatureReady(player2, new AinokTracker());
        blocker.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Ainok Tracker");
        harness.assertNotOnBattlefield(player2, "Ainok Tracker");
        harness.assertInGraveyard(player1, "Ainok Tracker");
        harness.assertInGraveyard(player2, "Ainok Tracker");
    }

    @Test
    void turningFaceUpBeforeCombatRestoresFirstStrike() {
        Permanent attacker = addCreatureReady(player1, new AinokTracker());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent blocker = addCreatureReady(player2, new AinokTracker());
        blocker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.turnFaceUp(player1, 0);
        assertThat(attacker.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Ainok Tracker");
        harness.assertInGraveyard(player2, "Ainok Tracker");
        assertThat(attacker.getMarkedDamage()).isZero();
    }
}
