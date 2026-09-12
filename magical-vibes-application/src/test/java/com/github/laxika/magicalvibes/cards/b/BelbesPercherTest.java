package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cloudskate;
import com.github.laxika.magicalvibes.cards.r.RathiFiend;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelbesPercher.class, Cloudskate.class, RathiFiend.class})
class BelbesPercherTest extends BaseCardTest {

    @Test
    @DisplayName("Belbe's Percher cannot block a creature without flying")
    void cannotBlockCreatureWithoutFlying() {
        Permanent attacker = addCreatureReady(player1, new RathiFiend());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BelbesPercher());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Belbe's Percher can block a creature with flying")
    void canBlockCreatureWithFlying() {
        Permanent attacker = addCreatureReady(player1, new Cloudskate());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BelbesPercher());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
    }
}
