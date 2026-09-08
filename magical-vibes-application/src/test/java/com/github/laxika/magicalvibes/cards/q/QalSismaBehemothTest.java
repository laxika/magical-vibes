package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QalSismaBehemoth.class, GrizzlyBears.class})
class QalSismaBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without paying {2}")
    void cannotAttackWithoutPayment() {
        Permanent behemoth = addCreatureReady(player1, new QalSismaBehemoth());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(behemoth.isAttacking()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacks when its controller pays {2}")
    void attacksWhenPaid() {
        Permanent behemoth = addCreatureReady(player1, new QalSismaBehemoth());
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(player1, List.of(0));

        assertThat(gd.creaturesAttackedCountThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot block without paying {2}")
    void cannotBlockWithoutPayment() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent behemoth = addCreatureReady(player2, new QalSismaBehemoth());
        harness.addMana(player2, ManaColor.RED, 1);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(behemoth.isBlocking()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocks when its controller pays {2}")
    void blocksWhenPaid() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent behemoth = addCreatureReady(player2, new QalSismaBehemoth());
        harness.addMana(player2, ManaColor.RED, 2);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(behemoth.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
