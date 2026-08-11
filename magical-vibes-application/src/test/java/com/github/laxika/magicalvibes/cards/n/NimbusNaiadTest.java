package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NimbusNaiadTest extends BaseCardTest {

    @Test
    @DisplayName("Nimbus Naiad can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new NimbusNaiad()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent naiad = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, naiad)).isTrue();
    }

    @Test
    @DisplayName("Nimbus Naiad can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NimbusNaiad()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent naiad = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.isCreature(gd, naiad)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Nimbus Naiad becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NimbusNaiad()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent naiad = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(naiad);
        assertThat(gqs.isCreature(gd, naiad)).isTrue();
        assertThat(naiad.isAttached()).isFalse();
    }
}
