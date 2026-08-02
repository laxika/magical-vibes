package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LushGrowth;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A land given several basic land types at once (Lush Growth: Mountain, Forest and Plains) taps for
 * one mana of any of those colors. The castable preview has to offer every one of them while still
 * counting the land as the single mana one tap yields.
 */
class MultiTypeLandManaPlayabilityTest extends BaseCardTest {

    private GameActionAvailabilityService availability() {
        return harness.getGameActionAvailabilityService();
    }

    private void addLushGrowthForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new LushGrowth());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private List<Integer> potentiallyPlayable() {
        GameActionAvailabilityService svc = availability();
        return svc.getPotentialPlayableCardIndices(gd, player1.getId(),
                svc.getPlayableCardIndices(gd, player1.getId()));
    }

    @Test
    @DisplayName("A type-replaced land pays a pip of one of its granted colors")
    void multiTypeLandPaysAGrantedColorPip() {
        addLushGrowthForest();
        harness.setHand(player1, List.of(new WildGrowth()));

        assertThat(potentiallyPlayable()).containsExactly(0);
    }

    @Test
    @DisplayName("A type-replaced land is still only one mana")
    void multiTypeLandCountsAsASingleMana() {
        addLushGrowthForest();
        harness.setHand(player1, List.of(new WildGrowth(), new GrizzlyBears()));

        assertThat(availability().getPotentialManaTotal(gd, player1.getId())).isEqualTo(1);
        assertThat(potentiallyPlayable())
                .as("Wild Growth {G} is affordable; Grizzly Bears {1}{G} needs a second mana")
                .containsExactly(0);
    }
}
