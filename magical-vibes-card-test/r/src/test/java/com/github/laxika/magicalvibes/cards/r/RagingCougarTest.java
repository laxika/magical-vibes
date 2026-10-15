package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RagingCougar.class})
class RagingCougarTest extends BaseCardTest {

    @Test
    void canAttackTheTurnItEntersTheBattlefield() {
        harness.castFromHand(player1, new RagingCougar(), "{2}{R}");
        harness.passBothPriorities();

        Permanent cougar = findPermanent(player1, "Raging Cougar");
        assertThat(cougar.isSummoningSick()).isTrue();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(cougar.isTapped()).isTrue();
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotAttackWhileTappedDespiteHaste() {
        harness.castFromHand(player1, new RagingCougar(), "{2}{R}");
        harness.passBothPriorities();

        Permanent cougar = findPermanent(player1, "Raging Cougar");
        cougar.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        harness.assertLife(player2, 20);
    }
}
