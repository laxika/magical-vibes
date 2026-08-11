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

class CelestialArchonTest extends BaseCardTest {

    @Test
    @DisplayName("Celestial Archon can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new CelestialArchon()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent archon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, archon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, archon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, archon)).isEqualTo(4);
    }

    @Test
    @DisplayName("Celestial Archon can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialArchon()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent archon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.isCreature(gd, archon)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Celestial Archon becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialArchon()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent archon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(archon);
        assertThat(gqs.isCreature(gd, archon)).isTrue();
        assertThat(archon.isAttached()).isFalse();
    }
}
