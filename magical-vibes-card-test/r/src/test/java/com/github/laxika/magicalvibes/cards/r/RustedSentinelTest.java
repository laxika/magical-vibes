package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RustedSentinel.class})
class RustedSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Rusted Sentinel enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RustedSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rusted Sentinel");
        Permanent sentinel = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rusted Sentinel enters tapped without being cast")
    void entersTappedWithoutBeingCast() {
        Permanent sentinel = harness.enterBattlefieldAndReturn(player2, new RustedSentinel());

        harness.assertOnBattlefield(player2, "Rusted Sentinel");
        assertThat(sentinel.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rusted Sentinel untaps normally during its controller's untap step")
    void untapsNormally() {
        Permanent sentinel = harness.enterBattlefieldAndReturn(player1, new RustedSentinel());
        assertThat(sentinel.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(sentinel.isTapped()).isFalse();
    }
}
