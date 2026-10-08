package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenInfiltrator.class, OrnithopterOfParadise.class})
class VedalkenInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Two artifacts do not enable metalcraft")
    void twoArtifactsDoNotEnableMetalcraft() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new VedalkenInfiltrator());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+0 with three artifacts")
    void getsBoostWithThreeArtifacts() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new VedalkenInfiltrator());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the boost when artifact count drops below three")
    void losesBoostWhenArtifactRemoved() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new VedalkenInfiltrator());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());
        Permanent thirdArtifact = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(thirdArtifact);

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent artifacts do not count for metalcraft")
    void opponentArtifactsDoNotCount() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new VedalkenInfiltrator());
        harness.addToBattlefield(player2, new OrnithopterOfParadise());
        harness.addToBattlefield(player2, new OrnithopterOfParadise());
        harness.addToBattlefield(player2, new OrnithopterOfParadise());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = addCreatureReady(player2, new OrnithopterOfParadise());
        Permanent infiltrator = addCreatureReady(player1, new VedalkenInfiltrator());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(infiltrator);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Additional artifacts do not increase the metalcraft bonus")
    void fourArtifactsStillGiveOnlyOnePower() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new VedalkenInfiltrator());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new OrnithopterOfParadise());
        }

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapped artifact creatures count and are not boosted by metalcraft")
    void tappedArtifactsCountWithoutReceivingBoost() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new VedalkenInfiltrator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
        artifact.tap();
        harness.addToBattlefield(player1, new OrnithopterOfParadise());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, artifact)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unblockability remains active with metalcraft")
    void cannotBeBlockedWithMetalcraft() {
        Permanent blocker = addCreatureReady(player2, new OrnithopterOfParadise());
        Permanent infiltrator = addCreatureReady(player1, new VedalkenInfiltrator());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new OrnithopterOfParadise());
        }
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(infiltrator);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
