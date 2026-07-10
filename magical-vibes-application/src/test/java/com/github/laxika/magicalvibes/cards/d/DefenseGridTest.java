package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefenseGridTest extends BaseCardTest {

    @Test
    @DisplayName("A spell is not taxed during its controller's own turn")
    void notTaxedOnOwnTurn() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        // {R} is enough on the caster's own turn
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("A spell cast on an opponent's turn costs {3} more")
    void taxedOnOpponentsTurn() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.forceActivePlayer(player1);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        // {R} is not enough during player1's turn — needs {3}{R}
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A spell on an opponent's turn is castable with the extra {3}")
    void castableWithExtraManaOnOpponentsTurn() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.forceActivePlayer(player1);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("The tax also applies to the controller's own spells on an opponent's turn")
    void controllerAlsoTaxedOffTurn() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Even Defense Grid's controller pays {3} more when it's not their turn
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
