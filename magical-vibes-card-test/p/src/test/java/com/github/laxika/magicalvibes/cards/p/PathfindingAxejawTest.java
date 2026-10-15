package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
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

@CardUsed({PathfindingAxejaw.class, Forest.class, Abrade.class, RayOfCommand.class})
class PathfindingAxejawTest extends BaseCardTest {

    @Test
    @DisplayName("ETB explore with a land puts it into hand")
    void exploreLandGoesToHand() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castPathfindingAxejaw();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(findPathfindingAxejaw().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB explore with a nonland puts a counter on the creature")
    void exploreNonlandAddsCounter() {
        Card nonland = new PathfindingAxejaw();
        harness.setLibrary(player1, List.of(nonland));

        castPathfindingAxejaw();

        assertThat(findPathfindingAxejaw().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("ETB explore can put a revealed nonland into the graveyard")
    void exploreNonlandMayGoToGraveyard() {
        Card nonland = new PathfindingAxejaw();
        harness.setLibrary(player1, List.of(nonland));

        castPathfindingAxejaw();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(nonland.getId()));
    }

    @Test
    @DisplayName("Declining explore's graveyard choice leaves the nonland on top")
    void exploreNonlandMayStayOnTop() {
        Card nonland = new PathfindingAxejaw();
        harness.setLibrary(player1, List.of(nonland));

        castPathfindingAxejaw();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nonland);
        assertThat(findPathfindingAxejaw().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exploring an empty library still puts a counter on the creature")
    void exploreEmptyLibraryAddsCounter() {
        harness.setLibrary(player1, List.of());

        castPathfindingAxejaw();

        assertThat(findPathfindingAxejaw().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @CardUsed(Abrade.class)
    @DisplayName("The entry trigger still explores after the creature dies")
    void exploreAfterSourceDies() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        castPathfindingAxejawWithoutResolvingTrigger();
        Permanent axejaw = findPathfindingAxejaw();
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player2, 0, 0, List.of(axejaw.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Pathfinding Axejaw");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed(RayOfCommand.class)
    @DisplayName("Explore uses the creature's current controller after control changes")
    void exploreUsesCurrentControllerLibrary() {
        Card originalControllersLand = new Forest();
        Card newControllersLand = new Forest();
        harness.setLibrary(player1, List.of(originalControllersLand));
        harness.setLibrary(player2, List.of(newControllersLand));
        castPathfindingAxejawWithoutResolvingTrigger();
        Permanent axejaw = findPathfindingAxejaw();
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0, axejaw.getId());
        harness.assertOnBattlefield(player2, "Pathfinding Axejaw");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(newControllersLand);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalControllersLand);
        assertThat(axejaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castPathfindingAxejaw() {
        castPathfindingAxejawWithoutResolvingTrigger();
        harness.passBothPriorities();
    }

    private void castPathfindingAxejawWithoutResolvingTrigger() {
        harness.setHand(player1, List.of(new PathfindingAxejaw()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findPathfindingAxejaw() {
        return findPermanent(player1, "Pathfinding Axejaw");
    }
}
