package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoonSatyrTest extends BaseCardTest {

    @Test
    @DisplayName("Boon Satyr can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent satyr = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, satyr)).isTrue();
    }

    @Test
    @DisplayName("Boon Satyr can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent satyr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.isCreature(gd, satyr)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("A bestowed Boon Satyr becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent satyr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(satyr);
        assertThat(gqs.isCreature(gd, satyr)).isTrue();
        assertThat(satyr.isAttached()).isFalse();
    }
}
