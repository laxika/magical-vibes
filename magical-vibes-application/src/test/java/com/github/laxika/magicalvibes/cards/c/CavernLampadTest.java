package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CavernLampadTest extends BaseCardTest {

    @Test
    @DisplayName("Cavern Lampad can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new CavernLampad()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent lampad = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, lampad)).isTrue();
    }

    @Test
    @DisplayName("Cavern Lampad can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CavernLampad()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent lampad = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.isCreature(gd, lampad)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Cavern Lampad becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CavernLampad()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent lampad = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lampad);
        assertThat(gqs.isCreature(gd, lampad)).isTrue();
        assertThat(lampad.isAttached()).isFalse();
    }
}
