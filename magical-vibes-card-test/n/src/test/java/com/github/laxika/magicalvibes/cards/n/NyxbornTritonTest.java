package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SwordwiseCentaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyxbornTriton.class, SwordwiseCentaur.class})
class NyxbornTritonTest extends BaseCardTest {

    @Test
    @DisplayName("Nyxborn Triton can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new NyxbornTriton()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent triton = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, triton)).isTrue();
    }

    @Test
    @DisplayName("Nyxborn Triton can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new NyxbornTriton()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
    }

    @Test
    @DisplayName("A bestowed Nyxborn Triton becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new NyxbornTriton()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent triton = findPermanent(player1, "Nyxborn Triton");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(triton);
        assertThat(gqs.isCreature(gd, triton)).isTrue();
        assertThat(triton.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature if its target leaves before resolution")
    void becomesCreatureWhenTargetLeavesBeforeResolution() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new NyxbornTriton()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.passBothPriorities();

        Permanent triton = findPermanent(player1, "Nyxborn Triton");
        assertThat(gqs.isCreature(gd, triton)).isTrue();
        assertThat(triton.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Nyxborn Triton");
    }

    @Test
    @DisplayName("Bestow can enchant an opponent's creature without changing controllers")
    void bestowsOnOpponentsCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        int otherPower = gqs.getEffectivePower(gd, other);
        int otherToughness = gqs.getEffectiveToughness(gd, other);
        harness.setHand(player1, List.of(new NyxbornTriton()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent triton = findPermanent(player1, "Nyxborn Triton");
        assertThat(triton.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, triton)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(host);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(otherPower);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(otherToughness);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(triton);
        assertThat(gqs.isCreature(gd, triton)).isTrue();
        assertThat(triton.isAttached()).isFalse();
    }
}
