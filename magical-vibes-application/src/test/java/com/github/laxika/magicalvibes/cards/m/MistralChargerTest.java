package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SimicRagworm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistralCharger.class, SimicRagworm.class})
class MistralChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        Permanent blocker = addCreatureReady(player2, new SimicRagworm());

        declareAttackersAndPrepareBlockers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(charger)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(charger);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Flying allows another flying creature to block Mistral Charger")
    void flyingAllowsAnotherFlyingCreatureToBlock() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        Permanent blocker = addCreatureReady(player2, new MistralCharger());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(charger))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mistral Charger can block a creature without flying")
    void flyingCreatureCanBlockGroundCreature() {
        Permanent attacker = addCreatureReady(player1, new SimicRagworm());
        Permanent charger = addCreatureReady(player2, new MistralCharger());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(charger),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(charger.isBlocking()).isTrue();
    }
}
