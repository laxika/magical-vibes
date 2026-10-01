package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MishrasBauble.class, SnowCoveredIsland.class, BorealDruid.class})
class MishrasBaubleTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at the target player's top card and schedules a draw at the next upkeep")
    void looksAndSchedulesDraw() {
        Permanent bauble = addBauble();
        Card topCard = new SnowCoveredIsland();
        Card nextCard = new BorealDruid();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bauble);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard);
        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller draws from their library at the next turn's upkeep")
    void drawsAtNextTurnUpkeep() {
        addBauble();
        Card drawn = new BorealDruid();
        Card remaining = new SnowCoveredIsland();
        Card targetTop = new SnowCoveredIsland();
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.setLibrary(player2, List.of(targetTop));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handBefore + 1)
                .contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(targetTop);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Rejects a non-player target")
    void rejectsNonPlayerTarget() {
        addBauble();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    private Permanent addBauble() {
        return addCreatureReady(player1, new MishrasBauble());
    }
}
