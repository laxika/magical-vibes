package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TerritorialDisputeTest extends BaseCardTest {

    private boolean controlsDispute(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(permanent -> permanent.getCard().getName().equals("Territorial Dispute"));
    }

    @Test
    @DisplayName("Without a land to sacrifice, the upkeep trigger sacrifices Territorial Dispute")
    void sacrificesItWithoutLand() {
        harness.addToBattlefield(player1, new TerritorialDispute());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(controlsDispute(player1)).isFalse();
    }

    @Test
    @DisplayName("The upkeep trigger offers a land sacrifice")
    void offersLandSacrifice() {
        harness.addToBattlefield(player1, new TerritorialDispute());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Sacrificing a land keeps Territorial Dispute on the battlefield")
    void sacrificingLandKeepsIt() {
        harness.addToBattlefield(player1, new TerritorialDispute());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsDispute(player1)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Declining the land sacrifice sacrifices Territorial Dispute")
    void decliningLandSacrificeSacrificesIt() {
        harness.addToBattlefield(player1, new TerritorialDispute());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(controlsDispute(player1)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Territorial Dispute prevents every player from playing lands")
    void preventsAllLandPlays() {
        harness.addToBattlefield(player1, new TerritorialDispute());

        for (Player player : List.of(player1, player2)) {
            harness.forceActivePlayer(player);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player, List.of(new Forest()));
            harness.clearPriorityPassed();
            harness.ensurePriority(player);

            assertThat(harness.getGameActionAvailabilityService()
                    .getPlayableCardIndices(gd, player.getId())).isEmpty();
        }
    }
}
