package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlitterstepEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Flitterstep Eidolon can be cast normally and can't be blocked")
    void castsNormallyAsUnblockableCreature() {
        harness.setHand(player1, List.of(new FlitterstepEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, eidolon)).isTrue();
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature and makes it unblockable")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlitterstepEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, bear)).isTrue();
    }
}
