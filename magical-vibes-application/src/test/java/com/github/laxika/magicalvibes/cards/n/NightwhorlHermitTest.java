package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightwhorlHermit.class, GrizzlyBears.class})
class NightwhorlHermitTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold gives Nightwhorl Hermit +1/+0 and makes it unable to be blocked")
    void thresholdAppliesPowerBoostAndUnblockable() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent hermit = addCreatureReady(player1, new NightwhorlHermit());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        hermit.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, hermit)).isEqualTo(2);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(hermit);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Without threshold, Nightwhorl Hermit has no bonus and can be blocked")
    void thresholdIsRequired() {
        Permanent hermit = addCreatureReady(player1, new NightwhorlHermit());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        hermit.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, hermit)).isEqualTo(1);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(hermit);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
