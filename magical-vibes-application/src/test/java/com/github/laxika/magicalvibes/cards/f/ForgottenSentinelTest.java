package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgottenSentinel.class})
class ForgottenSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Forgotten Sentinel enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ForgottenSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forgotten Sentinel");
        Permanent sentinel = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Forgotten Sentinel enters tapped even when it is not cast")
    void entersTappedWithoutBeingCast() {
        Permanent sentinel = harness.enterBattlefieldAndReturn(player1, new ForgottenSentinel());

        harness.assertOnBattlefield(player1, "Forgotten Sentinel");
        assertThat(sentinel.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forgotten Sentinel untaps normally during its controller's untap step")
    void untapsNormally() {
        Permanent sentinel = harness.enterBattlefieldAndReturn(player1, new ForgottenSentinel());
        assertThat(sentinel.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(sentinel.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(sentinel.isTapped()).isFalse();
    }
}
