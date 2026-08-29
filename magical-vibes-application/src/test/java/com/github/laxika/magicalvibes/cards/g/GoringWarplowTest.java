package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoringWarplowTest extends BaseCardTest {

    @Test
    @DisplayName("Prototype cast uses the alternate characteristics and keeps deathtouch")
    void prototypeCastUsesAlternateCharacteristics() {
        harness.setHand(player1, List.of(new GoringWarplow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent warplow = findPermanent(player1, "Goring Warplow");
        assertThat(gqs.getEffectivePower(gd, warplow)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warplow)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, warplow)).containsExactly(CardColor.BLACK);
        assertThat(gqs.hasKeyword(gd, warplow, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Goring Warplow's deathtouch destroys a larger blocker")
    void deathtouchDestroysLargerBlocker() {
        Permanent warplow = harness.addToBattlefieldAndReturn(player1, new GoringWarplow());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        warplow.setSummoningSick(false);
        warplow.setAttacking(true);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(warplow.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }
}
