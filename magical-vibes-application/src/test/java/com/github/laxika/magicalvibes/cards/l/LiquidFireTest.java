package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LiquidFireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to the creature and 5 minus X to its controller")
    void dealsSplitDamage() {
        harness.addToBattlefield(player2, new CrawWurm());
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Craw Wurm");
        harness.castSorcery(player1, 0, 2, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Craw Wurm");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Supports both ends of the chosen range")
    void supportsZeroAndFive() {
        harness.addToBattlefield(player2, new CrawWurm());
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Craw Wurm");
        harness.castSorcery(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Craw Wurm");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);

        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);
        targetId = harness.getPermanentId(player2, "Craw Wurm");
        harness.castSorcery(player1, 0, 5, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Craw Wurm");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Rejects a chosen value outside the printed range")
    void rejectsOutOfRangeChoice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 6, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }
}
