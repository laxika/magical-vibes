package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhituEmbercoiler.class, GrizzlyBears.class, AirElemental.class})
class GhituEmbercoilerTest extends BaseCardTest {

    @Test
    @DisplayName("First main phase discard seeks and exiles a random card with greater mana value")
    void discardingSeeksGreaterManaValueCard() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        Card discarded = new GrizzlyBears();
        Card equalManaValue = new GrizzlyBears();
        Card greaterManaValue = new AirElemental();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(equalManaValue, greaterManaValue));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(greaterManaValue);
        assertThat(gd.exilePlayPermissions).containsEntry(greaterManaValue.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(greaterManaValue.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equalManaValue);
    }

    @Test
    @DisplayName("Declining the first main phase ability keeps the hand unchanged")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        Card cardInHand = new GrizzlyBears();
        Card libraryCard = new AirElemental();
        harness.setHand(player1, List.of(cardInHand));
        harness.setLibrary(player1, List.of(libraryCard));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No card is exiled when the library has no greater mana value card")
    void noGreaterCardDoesNothingAfterDiscard() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        Card discarded = new GrizzlyBears();
        Card equalManaValue = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(equalManaValue));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equalManaValue);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
