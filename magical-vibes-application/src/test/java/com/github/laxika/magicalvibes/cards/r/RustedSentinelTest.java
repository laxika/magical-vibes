package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
