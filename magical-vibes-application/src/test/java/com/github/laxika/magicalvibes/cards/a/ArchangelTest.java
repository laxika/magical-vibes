package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Archangel.class, Warthog.class})
class ArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a creature without flying from blocking Archangel")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new Archangel());
        addCreatureReady(player2, new Warthog());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Flying allows another creature with flying to block Archangel")
    void flyingAllowsFlyingCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new Archangel());
        Permanent blocker = addCreatureReady(player2, new Archangel());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Archangel untapped after attacking")
    void vigilanceKeepsArchangelUntappedAfterAttacking() {
        Permanent angel = addCreatureReady(player1, new Archangel());

        declareAttackers(List.of(0));

        assertThat(angel.isTapped()).isFalse();
    }
}
