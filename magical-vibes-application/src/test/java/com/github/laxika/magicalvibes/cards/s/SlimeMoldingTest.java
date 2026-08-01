package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlimeMoldingTest extends BaseCardTest {

    private List<Permanent> oozes() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Ooze"))
                .toList();
    }

    @Test
    @DisplayName("X=3 creates a single 3/3 Ooze token")
    void createsXByXOoze() {
        harness.setHand(player1, List.of(new SlimeMolding()));
        harness.addMana(player1, ManaColor.GREEN, 4); // X=3: {3}{G}

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(oozes()).hasSize(1);
        Permanent ooze = oozes().getFirst();
        assertThat(ooze.getCard().getPower()).isEqualTo(3);
        assertThat(ooze.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("X=1 creates a 1/1 Ooze token")
    void xOneCreatesOneOneOoze() {
        harness.setHand(player1, List.of(new SlimeMolding()));
        harness.addMana(player1, ManaColor.GREEN, 2); // X=1: {1}{G}

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(oozes()).hasSize(1);
        assertThat(oozes().getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(oozes().getFirst().getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("X=0 creates a 0/0 Ooze that dies immediately to state-based actions")
    void xZeroTokenDies() {
        harness.setHand(player1, List.of(new SlimeMolding()));
        harness.addMana(player1, ManaColor.GREEN, 1); // X=0: {0}{G}

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(oozes()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
