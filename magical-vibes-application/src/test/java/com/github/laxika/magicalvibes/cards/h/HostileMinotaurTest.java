package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HostileMinotaur.class})
class HostileMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield")
    void canAttackTheTurnItEnters() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new HostileMinotaur());
        harness.addToBattlefield(player2, new HostileMinotaur());

        declareAttackers(player1, List.of(0));

        assertThat(minotaur.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can attack immediately after being cast and resolved")
    void canAttackAfterResolving() {
        harness.castFromHand(player1, new HostileMinotaur(), "{3}{R}");
        harness.passBothPriorities();
        Permanent minotaur = findPermanent(player1, "Hostile Minotaur");
        harness.addToBattlefield(player2, new HostileMinotaur());

        declareAttackers(player1, List.of(0));

        assertThat(minotaur.isAttacking()).isTrue();
        assertThat(minotaur.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Haste does not allow a tapped creature to attack")
    void cannotAttackWhileTapped() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new HostileMinotaur());
        minotaur.tap();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(minotaur.isAttacking()).isFalse();
    }
}
