package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireAndBrimstone.class})
class FireAndBrimstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a player who attacked this turn and 4 damage to its controller")
    void damagesAttackingPlayerAndController() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        harness.setHand(player1, List.of(new FireAndBrimstone()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot target a player who did not attack this turn")
    void rejectsPlayerWhoDidNotAttack() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        harness.setHand(player1, List.of(new FireAndBrimstone()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Does nothing if the target no longer attacked this turn when it resolves")
    void rechecksAttackRestrictionAtResolution() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        harness.setHand(player1, List.of(new FireAndBrimstone()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, player2.getId());
        gd.playersDeclaredAttackersThisTurn.clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
