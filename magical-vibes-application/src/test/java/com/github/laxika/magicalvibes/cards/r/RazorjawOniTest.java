package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazorjawOniTest extends BaseCardTest {

    @Test
    @DisplayName("Black creatures can't block")
    void blackCreaturesCannotBlock() {
        addRazorjawOni(player1);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new ScatheZombies());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Black creatures can't block");
    }

    @Test
    @DisplayName("Nonblack creatures can block")
    void nonblackCreaturesCanBlock() {
        addRazorjawOni(player1);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private Permanent addRazorjawOni(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new RazorjawOni());
    }
}
