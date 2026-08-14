package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HidetsugusSecondRiteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 10 damage when the targeted player has exactly 10 life")
    void dealsTenDamageAtExactlyTenLife() {
        harness.setLife(player2, 10);
        castRite(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isZero();
    }

    @Test
    @DisplayName("Does nothing when the targeted player does not have exactly 10 life")
    void doesNothingAtOtherLifeTotal() {
        harness.setLife(player2, 11);
        castRite(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Checks the targeted player's life total as the spell resolves")
    void checksLifeTotalAtResolution() {
        harness.setLife(player2, 10);
        castRite(player2.getId());
        harness.setLife(player2, 9);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Can target any player even when that player is not at 10 life")
    void canTargetPlayerAtOtherLifeTotal() {
        harness.setHand(player1, List.of(new HidetsugusSecondRite()));
        addMana();

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HidetsugusSecondRite()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRite(UUID targetId) {
        harness.setHand(player1, List.of(new HidetsugusSecondRite()));
        addMana();
        harness.castInstant(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
