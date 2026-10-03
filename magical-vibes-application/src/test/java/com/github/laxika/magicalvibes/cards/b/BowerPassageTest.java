package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MoonlightGeist;
import com.github.laxika.magicalvibes.cards.n.NettleSwine;
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

@CardUsed({BowerPassage.class, MoonlightGeist.class, NettleSwine.class})
class BowerPassageTest extends BaseCardTest {

    @Test
    @DisplayName("A flier can't block an attacker controlled by Bower Passage's controller")
    void flierCannotBlockControllersCreature() {
        addBowerPassage(player1);
        addCreatureReady(player1, new NettleSwine()).setAttacking(true);
        addCreatureReady(player2, new MoonlightGeist());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures with flying can't block creatures you control");
    }

    @Test
    @DisplayName("A flier can't block a flying attacker controlled by Bower Passage's controller either")
    void flierCannotBlockControllersFlier() {
        addBowerPassage(player1);
        addCreatureReady(player1, new MoonlightGeist()).setAttacking(true);
        addCreatureReady(player2, new MoonlightGeist());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures with flying can't block creatures you control");
    }

    @Test
    @DisplayName("A creature without flying can still block")
    void groundCreatureCanStillBlock() {
        addBowerPassage(player1);
        addCreatureReady(player1, new NettleSwine()).setAttacking(true);
        addCreatureReady(player2, new NettleSwine());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("The restriction only covers the controller's own creatures")
    void flierCanBlockOpponentsCreature() {
        addBowerPassage(player2);
        addCreatureReady(player1, new NettleSwine()).setAttacking(true);
        addCreatureReady(player2, new MoonlightGeist());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("The restriction ends when Bower Passage leaves the battlefield")
    void flierCanBlockAfterPassageLeaves() {
        Permanent passage = addBowerPassage(player1);
        addCreatureReady(player1, new NettleSwine()).setAttacking(true);
        addCreatureReady(player2, new MoonlightGeist());
        gd.playerBattlefields.get(player1.getId()).remove(passage);
        gd.playerGraveyards.get(player1.getId()).add(passage.getCard());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("The restriction follows Bower Passage's current controller")
    void restrictionFollowsCurrentController() {
        Permanent passage = addBowerPassage(player2);
        addCreatureReady(player1, new NettleSwine()).setAttacking(true);
        addCreatureReady(player2, new MoonlightGeist());
        gd.playerBattlefields.get(player2.getId()).remove(passage);
        gd.playerBattlefields.get(player1.getId()).add(passage);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures with flying can't block creatures you control");
    }

    @Test
    @DisplayName("A creature that has lost flying can block a ground attacker")
    void formerFlierCanBlock() {
        addBowerPassage(player1);
        addCreatureReady(player1, new NettleSwine()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MoonlightGeist());
        blocker.setLosesAllAbilitiesUntilEndOfTurn(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    private Permanent addBowerPassage(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new BowerPassage());
    }
}
