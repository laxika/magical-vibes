package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
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

@CardUsed({JolraelsCentaur.class, Incinerate.class, IronTuskElephant.class})
class JolraelsCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Shroud prevents Jolrael's Centaur from being targeted")
    void shroudPreventsTargeting() {
        Permanent centaur = addCreatureReady(player2, new JolraelsCentaur());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, centaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingWeakensNonFlankingBlocker() {
        Permanent centaur = addCreatureReady(player1, new JolraelsCentaur());
        centaur.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }
}
