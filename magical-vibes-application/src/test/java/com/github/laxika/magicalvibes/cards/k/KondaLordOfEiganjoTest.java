package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KondaLordOfEiganjoTest extends BaseCardTest {

    @Test
    @DisplayName("Konda gets +5/+5 when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        konda.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, konda)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, konda)).isEqualTo(8);
    }

    @Test
    @DisplayName("Konda gets +5/+5 when it blocks")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent konda = addCreatureReady(player2, new KondaLordOfEiganjo());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, konda)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, konda)).isEqualTo(8);
    }

    @Test
    @DisplayName("Konda's indestructible survives a destroy effect")
    void indestructibleSurvivesDestroyEffect() {
        Permanent konda = addCreatureReady(player2, new KondaLordOfEiganjo());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, konda.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Konda, Lord of Eiganjo");
        harness.assertNotInGraveyard(player2, "Konda, Lord of Eiganjo");
    }
}
