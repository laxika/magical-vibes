package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeeperOfTheAccord.class, GrizzlyBears.class, Plains.class})
class KeeperOfTheAccordTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Soldier and may search for a tapped basic Plains on an opponent's end step")
    void createsSoldierAndSearchesForPlains() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        advanceToEndStep(player2);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        harness.passBothPriorities();

        var soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(1);
        var soldier = soldiers.getFirst().getCard();
        assertThat(soldier.isToken()).isTrue();
        assertThat(soldier.hasType(CardType.CREATURE)).isTrue();
        assertThat(soldier.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(soldier.getPower()).isEqualTo(1);
        assertThat(soldier.getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when the opponent does not control more creatures or lands")
    void doesNotTriggerWithoutEitherAdvantage() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.setLibrary(player1, List.of(new Plains()));

        advanceToEndStep(player2);

        assertThat(soldierTokens(player1)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Rechecks both conditions when the triggers resolve")
    void rechecksConditionsAtResolution() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.setLibrary(player1, List.of(new Plains()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(soldierTokens(player1)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plains"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger during the controller's own end step")
    void doesNotTriggerOnControllersEndStep() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.setLibrary(player1, List.of(new Plains()));

        advanceToEndStep(player1);

        assertThat(soldierTokens(player1)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Can decline the land search without suppressing the Soldier trigger")
    void canDeclineSearch() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new Plains());
        var plains = new Plains();
        harness.setLibrary(player1, List.of(plains));

        advanceToEndStep(player2);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(soldierTokens(player1)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
    }

    @Test
    @DisplayName("Creates a Soldier when only the creature condition is met")
    void createsSoldierWithoutLandAdvantage() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new KeeperOfTheAccord());

        advanceToEndStep(player2);

        assertThat(soldierTokens(player1)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searches for a tapped Plains when only the land condition is met")
    void searchesWithoutCreatureAdvantage() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new Plains());
        harness.setLibrary(player1, List.of(new Plains()));

        advanceToEndStep(player2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        assertThat(soldierTokens(player1)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting a search with no basic Plains does not put another card onto the battlefield")
    void searchWithNoMatchingCard() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new Plains());
        var creature = new KeeperOfTheAccord();
        harness.setLibrary(player1, List.of(creature));

        advanceToEndStep(player2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Keeper of the Accord")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private long soldierTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Soldier"))
                .count();
    }
}
