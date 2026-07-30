package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class BowerPassageTest extends BaseCardTest {

    @Test
    @DisplayName("A flier can't block an attacker controlled by Bower Passage's controller")
    void flierCannotBlockControllersCreature() {
        addBowerPassage(player1);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures with flying can't block creatures you control");
    }

    @Test
    @DisplayName("A flier can't block a flying attacker controlled by Bower Passage's controller either")
    void flierCannotBlockControllersFlier() {
        addBowerPassage(player1);
        addCreatureReady(player1, new AirElemental()).setAttacking(true);
        addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures with flying can't block creatures you control");
    }

    @Test
    @DisplayName("A creature without flying can still block")
    void groundCreatureCanStillBlock() {
        addBowerPassage(player1);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("The restriction only covers the controller's own creatures")
    void flierCanBlockOpponentsCreature() {
        addBowerPassage(player2);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private Permanent addBowerPassage(Player controller) {
        Permanent perm = new Permanent(new BowerPassage());
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }
}
