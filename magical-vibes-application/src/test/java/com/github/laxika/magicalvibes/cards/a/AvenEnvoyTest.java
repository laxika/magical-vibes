package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CloudreachCavalry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenEnvoy.class, CloudreachCavalry.class})
class AvenEnvoyTest extends BaseCardTest {

    @Test
    @DisplayName("Aven Envoy cannot be blocked by a creature without flying")
    void cannotBeBlockedByNonFlyingCreature() {
        addCreatureReady(player1, new AvenEnvoy());
        addCreatureReady(player2, new CloudreachCavalry());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Aven Envoy (flying)");
    }

    @Test
    @DisplayName("Aven Envoy can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new AvenEnvoy());
        Permanent blocker = addCreatureReady(player2, new AvenEnvoy());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
