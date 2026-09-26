package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfTombstones.class, GrizzlyBears.class, Plains.class})
class WallOfTombstonesTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger sets base toughness to one plus own graveyard creature cards")
    void setsBaseToughnessAtUpkeep() {
        Permanent wall = addWallReady(player1);
        wall.setToughnessModifier(1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Plains()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each controller upkeep snapshots the current creature-card count")
    void updatesSnapshotOnEachControllerUpkeep() {
        Permanent wall = addWallReady(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Plains()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only triggers during the wall controller's upkeep")
    void onlyTriggersDuringControllerUpkeep() {
        Permanent wall = addWallReady(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(1);
    }

    private Permanent addWallReady(Player player) {
        return addCreatureReady(player, new WallOfTombstones());
    }

}
