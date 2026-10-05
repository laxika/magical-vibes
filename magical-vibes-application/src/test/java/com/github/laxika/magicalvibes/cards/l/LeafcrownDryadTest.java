package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeafcrownDryad.class, TravelingPhilosopher.class})
class LeafcrownDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Leafcrown Dryad can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.castFromHand(player1, new LeafcrownDryad(), "{1}{G}");
        harness.passBothPriorities();

        Permanent dryad = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, dryad)).isTrue();
    }

    @Test
    @DisplayName("Leafcrown Dryad's bestow gives the enchanted creature +2/+2 and reach")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
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
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
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

    @Test
    @DisplayName("Bestow resolves as an unattached creature when its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new LeafcrownDryad()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.passBothPriorities();

        Permanent dryad = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, dryad)).isTrue();
        assertThat(dryad.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Leafcrown Dryad");
    }

    @Test
    @DisplayName("Bestow can enchant an opponent's creature without changing the Dryad's controller")
    void bestowsOnOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new LeafcrownDryad()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent dryad = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, dryad)).isFalse();
        assertThat(dryad.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dryad);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(dryad);
        assertThat(gqs.isCreature(gd, dryad)).isTrue();
        assertThat(dryad.isAttached()).isFalse();
    }

    @Test
    @DisplayName("The normal casting cost is insufficient to cast Leafcrown Dryad for bestow")
    void requiresFullBestowCost() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new LeafcrownDryad()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Leafcrown Dryad");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the bestowed Dryad removes the host's bonus and reach")
    void removingAuraRemovesBonus() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new LeafcrownDryad()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent dryad = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dryad));
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isFalse();
        harness.assertInGraveyard(player1, "Leafcrown Dryad");
    }
}
