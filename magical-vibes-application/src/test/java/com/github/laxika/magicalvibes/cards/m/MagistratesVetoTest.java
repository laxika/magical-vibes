package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DrakeHatchling;
import com.github.laxika.magicalvibes.cards.g.Groundskeeper;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.cards.k.KyrenSniper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagistratesVeto.class, JhovallRider.class, DrakeHatchling.class,
        Groundskeeper.class, KyrenSniper.class})
class MagistratesVetoTest extends BaseCardTest {

    @Test
    @DisplayName("White creatures cannot block")
    void whiteCreaturesCannotBlock() {
        assertCannotBlock(new JhovallRider());
    }

    @Test
    @DisplayName("Blue creatures cannot block")
    void blueCreaturesCannotBlock() {
        assertCannotBlock(new DrakeHatchling());
    }

    @Test
    @DisplayName("White creatures cannot block an opponent's attacker")
    void whiteCreaturesCannotBlockOpponentsAttacker() {
        harness.addToBattlefield(player1, new MagistratesVeto());
        Permanent blocker = addCreatureReady(player1, new JhovallRider());
        Permanent attacker = addCreatureReady(player2, new KyrenSniper());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("White creatures and blue creatures can't block");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Creatures that are neither white nor blue can block")
    void otherColorsCanBlock() {
        harness.addToBattlefield(player1, new MagistratesVeto());
        Permanent attacker = addCreatureReady(player1, new KyrenSniper());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Groundskeeper());

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void assertCannotBlock(Card blocker) {
        harness.addToBattlefield(player1, new MagistratesVeto());
        Permanent attacker = addCreatureReady(player1, new KyrenSniper());
        attacker.setAttacking(true);
        addCreatureReady(player2, blocker);

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("White creatures and blue creatures can't block");
    }
}
