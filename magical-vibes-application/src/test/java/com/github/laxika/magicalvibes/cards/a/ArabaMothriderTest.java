package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArabaMothriderTest extends BaseCardTest {

    @Test
    @DisplayName("When Araba Mothrider becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent mothrider = addReady(player1, new ArabaMothrider());
        mothrider.setAttacking(true);
        addReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(mothrider.getPowerModifier()).isEqualTo(1);
        assertThat(mothrider.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Araba Mothrider blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent mothrider = addReady(player2, new ArabaMothrider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(mothrider.getPowerModifier()).isEqualTo(1);
        assertThat(mothrider.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Araba Mothrider is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent mothrider = addReady(player1, new ArabaMothrider());
        mothrider.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(mothrider.getPowerModifier()).isZero();
        assertThat(mothrider.getToughnessModifier()).isZero();
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player,
                               com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
