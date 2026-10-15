package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PortentTracker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShivanBranchBurner.class, PortentTracker.class})
class ShivanBranchBurnerTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to help pay the generic cost")
    void castsWithConvoke() {
        List<Permanent> convokeCreatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new PortentTracker()),
                harness.addToBattlefieldAndReturn(player1, new PortentTracker()),
                harness.addToBattlefieldAndReturn(player1, new PortentTracker()),
                harness.addToBattlefieldAndReturn(player1, new PortentTracker()),
                harness.addToBattlefieldAndReturn(player1, new PortentTracker()));
        harness.setHand(player1, List.of(new ShivanBranchBurner()));
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                convokeCreatures.stream().map(Permanent::getId).toList());

        assertThat(convokeCreatures).allMatch(Permanent::isTapped);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Shivan Branch-Burner")).isEqualTo(1);
    }

    @Test
    @DisplayName("Red creatures can convoke both red mana symbols")
    void redCreaturesPayColoredCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ShivanBranchBurner());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ShivanBranchBurner());
        harness.setHand(player1, List.of(new ShivanBranchBurner()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Shivan Branch-Burner")).isEqualTo(3);
    }

    @Test
    @DisplayName("A summoning-sick creature can convoke")
    void summoningSickCreatureCanConvoke() {
        harness.castFromHand(player1, new PortentTracker(), "{1}{G}");
        harness.passBothPriorities();
        Permanent tracker = findPermanent(player1, "Portent Tracker");
        assertThat(tracker.isSummoningSick()).isTrue();
        harness.setHand(player1, List.of(new ShivanBranchBurner()));
        harness.addMana(player1, ManaColor.RED, 6);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(tracker.getId()));
        harness.passBothPriorities();

        assertThat(tracker.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Shivan Branch-Burner");
    }

    @Test
    @DisplayName("Haste allows attacking immediately and flying excludes ground blockers")
    void newlyCastDragonAttacksAndRejectsGroundBlocker() {
        harness.castFromHand(player1, new ShivanBranchBurner(), "{5}{R}{R}");
        harness.passBothPriorities();
        Permanent dragon = findPermanent(player1, "Shivan Branch-Burner");
        addCreatureReady(player2, new PortentTracker());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(dragon.isAttacking()).isTrue();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying creatures can block Shivan Branch-Burner")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new ShivanBranchBurner());
        Permanent blocker = addCreatureReady(player2, new ShivanBranchBurner());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
