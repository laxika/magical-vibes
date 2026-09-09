package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LimitedResources.class, CityOfTraitors.class})
class LimitedResourcesTest extends BaseCardTest {

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new CityOfTraitors());
        }
    }

    private List<java.util.UUID> landIds(Player player, int count) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CityOfTraitors)
                .limit(count)
                .map(Permanent::getId)
                .toList();
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CityOfTraitors)
                .count();
    }

    private void castLimitedResources() {
        harness.castFromHand(player1, new LimitedResources(), "{W}");
    }

    private List<Integer> playableLandIndices(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new CityOfTraitors()));
        harness.clearPriorityPassed();
        harness.ensurePriority(player);
        return harness.getGameActionAvailabilityService().getPlayableCardIndices(gd, player.getId());
    }

    @Test
    @DisplayName("Each player keeps up to five lands, choosing which excess lands to sacrifice")
    void sacrificesExcessLandsDuringResolution() {
        addLands(player1, 7);
        addLands(player2, 8);
        harness.addToBattlefield(player1, new LimitedResources());
        harness.forceActivePlayer(player1);

        castLimitedResources();
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 2));

        choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 3));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(5);
        assertThat(landCount(player2)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof LimitedResources)
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Players with five or fewer lands do not have to choose or sacrifice")
    void leavesPlayersWithFiveOrFewerLandsAlone() {
        addLands(player1, 3);
        addLands(player2, 5);

        castLimitedResources();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(3);
        assertThat(landCount(player2)).isEqualTo(5);
    }

    @Test
    @DisplayName("Players can't play lands when ten lands are on the battlefield")
    void preventsLandPlaysAtTenLands() {
        addLands(player1, 5);
        addLands(player2, 5);
        castLimitedResources();
        resolveAllTriggers();

        for (Player player : List.of(player1, player2)) {
            assertThat(playableLandIndices(player)).isEmpty();
        }

        harness.addToBattlefield(player1, new CityOfTraitors());
        for (Player player : List.of(player1, player2)) {
            assertThat(playableLandIndices(player)).isEmpty();
        }
    }

    @Test
    @DisplayName("Land plays remain available below ten lands")
    void allowsLandPlaysBelowThreshold() {
        addLands(player1, 5);
        addLands(player2, 4);
        castLimitedResources();
        resolveAllTriggers();

        for (Player player : List.of(player1, player2)) {
            assertThat(playableLandIndices(player)).contains(0);
        }
    }
}
