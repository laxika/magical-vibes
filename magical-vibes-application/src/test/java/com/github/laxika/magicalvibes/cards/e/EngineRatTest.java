package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EngineRatTest extends BaseCardTest {

    @Test
    @DisplayName("{5}{B}: each opponent loses 2 life")
    void abilityMakesOpponentLoseLife() {
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new EngineRat());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(rat.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability requires {5}{B}")
    void abilityRequiresMana() {
        harness.addToBattlefieldAndReturn(player1, new EngineRat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
