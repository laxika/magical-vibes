package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DaggerclawImp;
import com.github.laxika.magicalvibes.cards.f.Flight;
import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stratozeppelid.class, DaggerclawImp.class, Gristleback.class, Flight.class})
class StratozeppelidTest extends BaseCardTest {

    @Test
    @DisplayName("Stratozeppelid can block a ground creature granted flying by an Aura")
    void canBlockCreatureWithGrantedFlying() {
        Permanent blocker = addCreatureReady(player2, new Stratozeppelid());
        Permanent attacker = addCreatureReady(player1, new Gristleback());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Flight());
        aura.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stratozeppelid cannot block after the attacker loses Aura-granted flying")
    void cannotBlockAfterGrantedFlyingIsLost() {
        Permanent blocker = addCreatureReady(player2, new Stratozeppelid());
        Permanent attacker = addCreatureReady(player1, new Gristleback());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Flight());
        aura.setAttachedTo(attacker.getId());
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Stratozeppelid can block a creature with flying")
    void canBlockCreatureWithFlying() {
        Permanent blocker = addCreatureReady(player2, new Stratozeppelid());
        Permanent attacker = addCreatureReady(player1, new DaggerclawImp());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stratozeppelid cannot block a creature without flying")
    void cannotBlockCreatureWithoutFlying() {
        Permanent blocker = addCreatureReady(player2, new Stratozeppelid());
        Permanent attacker = addCreatureReady(player1, new Gristleback());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }
}
