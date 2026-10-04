package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrashOfRhinoBeetles.class, Forest.class})
class CrashOfRhinoBeetlesTest extends BaseCardTest {

    @Test
    void remainsFiveFiveWithFewerThanTenLands() {
        addLands(player1, 9);
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());

        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(5);
    }

    @Test
    void getsPlusTenPlusTenAtTenLands() {
        addLands(player1, 10);
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());

        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(15);
    }

    @Test
    void losesBoostWhenControllerDropsBelowTenLands() {
        addLands(player1, 10);
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(5);
    }

    @Test
    void opponentsLandsDoNotCount() {
        addLands(player2, 10);
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());

        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(5);
    }

    @Test
    void gainsBoostImmediatelyWhenTenthLandEnters() {
        addLands(player1, 9);
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());

        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(5);

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void moreThanTenTappedLandsStillGiveOnlyOneBoost() {
        addLands(player1, 12);
        gd.playerBattlefields.get(player1.getId()).forEach(permanent -> permanent.tap());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(15);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(15);
    }

    @Test
    void boostedBeetlesTrampleOverUnboostedOpposingBeetles() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CrashOfRhinoBeetles());
        Permanent blocker = addCreatureReady(player2, new CrashOfRhinoBeetles());
        addLands(player1, 10);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 5, player2.getId(), 10));

        harness.assertLife(player2, 10);
        harness.assertInGraveyard(player2, "Crash of Rhino Beetles");
        harness.assertOnBattlefield(player1, "Crash of Rhino Beetles");
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
