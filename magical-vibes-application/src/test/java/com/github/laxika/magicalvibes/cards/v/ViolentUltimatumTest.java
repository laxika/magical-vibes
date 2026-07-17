package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViolentUltimatumTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.RED, 3);
        harness.addMana(player, ManaColor.GREEN, 2);
    }

    @Test
    @DisplayName("Destroys three target permanents")
    void destroysThreePermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        addMana(player1);

        List<UUID> targets = gd.playerBattlefields.get(player2.getId()).stream()
                .map(p -> p.getId()).toList();

        harness.castSorcery(player1, 0, targets);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Must target exactly three permanents")
    void mustTargetThree() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ViolentUltimatum()));
        addMana(player1);

        List<UUID> twoTargets = gd.playerBattlefields.get(player2.getId()).stream()
                .map(p -> p.getId()).toList();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, twoTargets))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target");
    }
}
