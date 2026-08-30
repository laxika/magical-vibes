package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExplosiveWelcomeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 and 3 damage to different targets, then adds three red mana")
    void dealsDamageAndAddsMana() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ExplosiveWelcome()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot choose the same target twice")
    void requiresDifferentTargets() {
        harness.setHand(player1, List.of(new ExplosiveWelcome()));
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
