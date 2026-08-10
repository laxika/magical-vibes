package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LimitedResourcesTest extends BaseCardTest {

    private void addForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private List<java.util.UUID> landIds(Player player, int count) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest)
                .limit(count)
                .map(Permanent::getId)
                .toList();
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest)
                .count();
    }

    private void castLimitedResources() {
        harness.setHand(player1, List.of(new LimitedResources()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0);
    }

    @Test
    @DisplayName("Each player keeps up to five lands, choosing which excess lands to sacrifice")
    void sacrificesExcessLandsDuringResolution() {
        addForests(player1, 7);
        addForests(player2, 3);
        harness.forceActivePlayer(player1);

        castLimitedResources();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(5);
        assertThat(landCount(player2)).isEqualTo(3);
    }

    @Test
    @DisplayName("Players can't play lands when ten lands are on the battlefield")
    void preventsLandPlaysAtTenLands() {
        addForests(player1, 5);
        addForests(player2, 5);
        castLimitedResources();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        for (Player player : List.of(player1, player2)) {
            harness.forceActivePlayer(player);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player, List.of(new Forest()));
            harness.clearPriorityPassed();
            harness.ensurePriority(player);
            assertThat(availability.getPlayableCardIndices(gd, player.getId())).isEmpty();
        }
    }

    @Test
    @DisplayName("Land plays remain available below ten lands")
    void allowsLandPlaysBelowThreshold() {
        addForests(player1, 5);
        addForests(player2, 4);
        castLimitedResources();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
    }
}
