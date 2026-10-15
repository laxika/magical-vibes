package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeScout;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorjawOni.class, RavingOniSlave.class, SakuraTribeScout.class})
class RazorjawOniTest extends BaseCardTest {

    @Test
    @DisplayName("Black creatures can't block")
    void blackCreaturesCannotBlock() {
        addRazorjawOni(player1);
        addCreatureReady(player1, new SakuraTribeScout()).setAttacking(true);
        addCreatureReady(player2, new RavingOniSlave());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Black creatures can't block");
    }

    @Test
    @DisplayName("Nonblack creatures can block")
    void nonblackCreaturesCanBlock() {
        addRazorjawOni(player1);
        addCreatureReady(player1, new SakuraTribeScout()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SakuraTribeScout());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Razorjaw Oni cannot block because it is black")
    void razorjawOniCannotBlock() {
        addCreatureReady(player1, new SakuraTribeScout()).setAttacking(true);
        addRazorjawOni(player2);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Black creatures can't block");
    }

    @Test
    @DisplayName("The Oni also prevents its controller's other black creatures from blocking")
    void controllersBlackCreaturesCannotBlock() {
        addCreatureReady(player1, new SakuraTribeScout()).setAttacking(true);
        addRazorjawOni(player2);
        addCreatureReady(player2, new RavingOniSlave());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Black creatures can't block");
    }

    @Test
    @DisplayName("Black creatures can block after Razorjaw Oni leaves the battlefield")
    void restrictionEndsWhenOniLeavesBattlefield() {
        Permanent oni = addRazorjawOni(player1);
        addCreatureReady(player1, new SakuraTribeScout()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RavingOniSlave());
        gd.playerBattlefields.get(player1.getId()).remove(oni);
        gd.playerGraveyards.get(player1.getId()).add(oni.getCard());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Razorjaw Oni can attack and be blocked by a nonblack creature")
    void oniCanAttackAndBeBlockedByNonblackCreature() {
        addCreatureReady(player1, new RazorjawOni());
        Permanent blocker = addCreatureReady(player2, new SakuraTribeScout());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
    private Permanent addRazorjawOni(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new RazorjawOni());
    }
}
