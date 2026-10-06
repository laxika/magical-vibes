package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoliathSpider.class, CourierHawk.class})
class GoliathSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Goliath Spider can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new CourierHawk());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GoliathSpider());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach allows ground blocking and does not give Goliath Spider evasion")
    void canBlockAnotherGoliathSpider() {
        addCreatureReady(player1, new GoliathSpider());
        Permanent blocker = addCreatureReady(player2, new GoliathSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped Goliath Spider cannot block a flying creature")
    void tappedSpiderCannotBlockFlyingCreature() {
        addCreatureReady(player1, new CourierHawk());
        Permanent blocker = addCreatureReady(player2, new GoliathSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        blocker.tap();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(blocker.isBlocking()).isFalse();
    }
}
