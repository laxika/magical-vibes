package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TowerWinder.class, CommandTower.class, GrizzlyBears.class})
class TowerWinderTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability searches the library for Command Tower")
    void searchesLibraryForCommandTower() {
        CommandTower commandTower = new CommandTower();
        Card grizzlyBears = new GrizzlyBears();
        setLibrary(commandTower, grizzlyBears);
        castTowerWinder();

        resolveEnterTheBattlefieldTrigger();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(commandTower.getId());

        harness.handleMultipleCardsChosen(player1, List.of(commandTower.getId()));

        harness.assertInHand(player1, "Command Tower");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(grizzlyBears);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability searches the graveyard for Command Tower")
    void searchesGraveyardForCommandTower() {
        CommandTower commandTower = new CommandTower();
        harness.setGraveyard(player1, List.of(commandTower));
        castTowerWinder();

        resolveEnterTheBattlefieldTrigger();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(commandTower.getId());

        harness.handleMultipleCardsChosen(player1, List.of(commandTower.getId()));

        harness.assertInHand(player1, "Command Tower");
        harness.assertNotInGraveyard(player1, "Command Tower");
    }

    @Test
    @DisplayName("The enter-the-battlefield ability ignores cards with other names")
    void ignoresOtherCards() {
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        setLibrary(grizzlyBears);
        castTowerWinder();

        resolveEnterTheBattlefieldTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(grizzlyBears);
    }

    private void castTowerWinder() {
        harness.setHand(player1, List.of(new TowerWinder()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTheBattlefieldTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
