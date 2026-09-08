package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
