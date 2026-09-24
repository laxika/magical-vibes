package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenInfiltrator.class, Spellbook.class, ShuFootSoldiers.class})
class VedalkenInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Has base 1/3 without metalcraft")
    void baseStatsWithoutMetalcraft() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new VedalkenInfiltrator());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+0 with three artifacts")
    void getsBoostWithThreeArtifacts() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new VedalkenInfiltrator());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the boost when artifact count drops below three")
    void losesBoostWhenArtifactRemoved() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new VedalkenInfiltrator());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent thirdArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(thirdArtifact);

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent artifacts do not count for metalcraft")
    void opponentArtifactsDoNotCount() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new VedalkenInfiltrator());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = addCreatureReady(player2, new ShuFootSoldiers());
        Permanent infiltrator = addCreatureReady(player1, new VedalkenInfiltrator());
        infiltrator.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(infiltrator);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
