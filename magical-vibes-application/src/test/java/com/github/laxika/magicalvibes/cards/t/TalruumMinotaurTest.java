package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalruumMinotaur.class})
class TalruumMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Talruum Minotaur to attack immediately after entering")
    void hasteAllowsImmediateAttack() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new TalruumMinotaur());
        assertThat(minotaur.isSummoningSick()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Haste allows Talruum Minotaur to attack the turn its spell resolves")
    void hasteAllowsAttackAfterCasting() {
        harness.castFromHand(player1, new TalruumMinotaur(), "{2}{R}{R}");
        harness.passBothPriorities();
        Permanent minotaur = findPermanent(player1, "Talruum Minotaur");
        assertThat(minotaur.isSummoningSick()).isTrue();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(minotaur.isTapped()).isTrue();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Haste does not allow a tapped Talruum Minotaur to attack")
    void hasteDoesNotAllowAttackWhileTapped() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new TalruumMinotaur());
        minotaur.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        harness.assertLife(player2, 20);
    }
}
