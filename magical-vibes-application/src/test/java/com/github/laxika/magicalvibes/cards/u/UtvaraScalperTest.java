package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UtvaraScalper.class})
class UtvaraScalperTest extends BaseCardTest {

    @Test
    @DisplayName("Utvara Scalper must attack each combat if able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new UtvaraScalper());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Utvara Scalper attacks when declared")
    void attacksWhenDeclared() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new UtvaraScalper());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Utvara Scalper does not need to attack while summoning sick")
    void doesNotHaveToAttackWithSummoningSickness() {
        Permanent scalper = harness.addToBattlefieldAndReturn(player1, new UtvaraScalper());

        declareAttackers(List.of());

        assertThat(scalper.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Utvara Scalper does not need to attack while tapped")
    void doesNotHaveToAttackWhenTapped() {
        Permanent scalper = addCreatureReady(player1, new UtvaraScalper());
        scalper.tap();

        declareAttackers(List.of());

        assertThat(scalper.isAttacking()).isFalse();
    }
}
