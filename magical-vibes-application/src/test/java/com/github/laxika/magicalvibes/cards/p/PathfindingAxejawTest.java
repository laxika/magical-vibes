package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathfindingAxejaw.class, Forest.class, GrizzlyBears.class})
class PathfindingAxejawTest extends BaseCardTest {

    @Test
    @DisplayName("ETB explore with a land puts it into hand")
    void exploreLandGoesToHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        castPathfindingAxejaw();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(findPathfindingAxejaw().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB explore with a nonland puts a counter on the creature")
    void exploreNonlandAddsCounter() {
        Card nonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castPathfindingAxejaw();

        assertThat(findPathfindingAxejaw().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("ETB explore can put a revealed nonland into the graveyard")
    void exploreNonlandMayGoToGraveyard() {
        Card nonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castPathfindingAxejaw();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(nonland.getId()));
    }

    private void castPathfindingAxejaw() {
        harness.setHand(player1, List.of(new PathfindingAxejaw()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPathfindingAxejaw() {
        return findPermanent(player1, "Pathfinding Axejaw");
    }
}
