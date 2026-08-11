package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeafcrownDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Leafcrown Dryad can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new LeafcrownDryad()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent dryad = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, dryad)).isTrue();
    }

    @Test
    @DisplayName("Leafcrown Dryad's bestow gives the enchanted creature +2/+2 and reach")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeafcrownDryad()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Leafcrown Dryad becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeafcrownDryad()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent dryad = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dryad);
        assertThat(gqs.isCreature(gd, dryad)).isTrue();
        assertThat(dryad.isAttached()).isFalse();
    }
}
