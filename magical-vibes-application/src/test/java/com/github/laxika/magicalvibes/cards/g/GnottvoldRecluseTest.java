package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BattlefieldRaptor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GnottvoldRecluse.class, BattlefieldRaptor.class})
class GnottvoldRecluseTest extends BaseCardTest {

    @Test
    void reachLetsGnottvoldRecluseBlockAcreatureWithFlying() {
        Permanent flyer = addCreatureReady(player1, new BattlefieldRaptor());
        flyer.setAttacking(true);
        Permanent recluse = addCreatureReady(player2, new GnottvoldRecluse());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(recluse.isBlocking()).isTrue();
    }

    @Test
    void reachAllowsBlockingNonFlyingCreature() {
        addCreatureReady(player1, new GnottvoldRecluse());
        Permanent recluse = addCreatureReady(player2, new GnottvoldRecluse());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(recluse.isBlocking()).isTrue();
    }

    @Test
    void tappedRecluseCannotBlockFlyingCreature() {
        addCreatureReady(player1, new BattlefieldRaptor());
        Permanent recluse = addCreatureReady(player2, new GnottvoldRecluse());

        declareAttackersAndPrepareBlockers(List.of(0));
        recluse.tap();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(recluse.isBlocking()).isFalse();
    }
}
