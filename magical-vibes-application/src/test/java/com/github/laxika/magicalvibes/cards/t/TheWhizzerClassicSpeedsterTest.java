package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.cards.h.HappyHoganDauntlessDriver;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWhizzerClassicSpeedster.class, HonorGuard.class, HappyHoganDauntlessDriver.class})
class TheWhizzerClassicSpeedsterTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills a 1/1 before it deals regular damage")
    void firstStrikeKillsBeforeRegularDamage() {
        addCreatureReady(player1, new TheWhizzerClassicSpeedster());
        addCreatureReady(player2, new HonorGuard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertOnBattlefield(player1, "The Whizzer, Classic Speedster");
        harness.assertInGraveyard(player2, "Honor Guard");
    }

    @Test
    @DisplayName("First strike prevents a killed blocker from dealing damage back")
    void firstStrikePreventsBlockerDamage() {
        Permanent attacker = addCreatureReady(player1, new TheWhizzerClassicSpeedster());
        addCreatureReady(player2, new HappyHoganDauntlessDriver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "The Whizzer, Classic Speedster");
        harness.assertInGraveyard(player2, "Happy Hogan, Dauntless Driver");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("First strike also prevents damage from an attacker killed while blocking")
    void firstStrikeWorksWhileBlocking() {
        addCreatureReady(player1, new HappyHoganDauntlessDriver());
        Permanent blocker = addCreatureReady(player2, new TheWhizzerClassicSpeedster());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Happy Hogan, Dauntless Driver");
        harness.assertOnBattlefield(player2, "The Whizzer, Classic Speedster");
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Haste allows attacking immediately after resolving")
    void hasteAllowsAttackingImmediately() {
        harness.setHand(player1, List.of(new TheWhizzerClassicSpeedster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
