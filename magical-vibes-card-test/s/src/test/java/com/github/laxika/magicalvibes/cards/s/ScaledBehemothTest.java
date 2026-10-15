package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScaledBehemoth.class, Shock.class, GiantGrowth.class})
class ScaledBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot target Scaled Behemoth with spells")
    void opponentCannotTargetWithSpells() {
        Permanent behemothPerm = addCreatureReady(player1, new ScaledBehemoth());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, behemothPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target own Scaled Behemoth with spells")
    void controllerCanTargetOwnBehemoth() {
        Permanent behemothPerm = addCreatureReady(player1, new ScaledBehemoth());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, behemothPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Hexproof does not prevent damage from the controller's targeted spell")
    void controllerCanDamageOwnBehemoth() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new ScaledBehemoth());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, behemoth.getId());
        harness.passBothPriorities();

        assertThat(behemoth.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Scaled Behemoth");
    }
}
