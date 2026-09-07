package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RevenantPatriarch.class)
class RevenantPatriarchTest extends BaseCardTest {

    @Test
    @DisplayName("Skips the targeted player's next combat phase when white mana was spent")
    void skipsCombatPhaseWhenWhiteManaWasSpent() {
        castRevenantPatriarch(ManaColor.WHITE, player2.getId());

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not skip a combat phase when white mana was not spent")
    void doesNotSkipCombatPhaseWithoutWhiteMana() {
        castRevenantPatriarch(ManaColor.COLORLESS, player2.getId());

        assertThat(gd.skipNextCombatPhaseCount).isEmpty();
    }

    @Test
    @DisplayName("May target either player")
    void mayTargetEitherPlayer() {
        castRevenantPatriarch(ManaColor.WHITE, player1.getId());

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot block")
    void cannotBlock() {
        Permanent blocker = new Permanent(new RevenantPatriarch());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        assertThat(bls.canBlock(gd, blocker)).isFalse();
    }

    private void castRevenantPatriarch(ManaColor extraManaColor, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RevenantPatriarch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, extraManaColor, 1);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
