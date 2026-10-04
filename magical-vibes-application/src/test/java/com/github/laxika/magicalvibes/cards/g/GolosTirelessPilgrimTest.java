package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CrypticCaves;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GolosTirelessPilgrim.class, CrypticCaves.class, Forest.class, Plains.class, GreenwoodSentinel.class})
class GolosTirelessPilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may search for any land and put it onto the battlefield tapped")
    void etbMaySearchForAnyLand() {
        harness.setHand(player1, List.of(new GolosTirelessPilgrim()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);

        Card searchableLand = new CrypticCaves();
        harness.setLibrary(player1, List.of(searchableLand, new Forest(), new GreenwoodSentinel()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards())
                .extracting(Card::getId)
                .contains(searchableLand.getId());
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));

        int choice = 0;
        for (int i = 0; i < search.params().cards().size(); i++) {
            if (search.params().cards().get(i).getId().equals(searchableLand.getId())) {
                choice = i;
                break;
            }
        }
        harness.handleCardChosen(player1, choice);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
    }

    @Test
    @DisplayName("Activated ability exiles the top three cards with free-play permission")
    void activatedAbilityExilesTopThreeForFree() {
        harness.addToBattlefield(player1, new GolosTirelessPilgrim());

        Card first = new Plains();
        Card second = new Forest();
        Card third = new GreenwoodSentinel();
        Card fourth = new CrypticCaves();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    void mayDeclineSearchWithoutChangingLibrary() {
        Card land = new Forest();
        Card creature = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(land, creature));
        harness.setHand(player1, List.of(new GolosTirelessPilgrim()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptedSearchMayFailToFindEvenWhenLandExists() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new GolosTirelessPilgrim()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void playsCreatureForFreeButDoesNotGrantExtraLandPlays() {
        harness.addToBattlefield(player1, new GolosTirelessPilgrim());
        Card creature = new GreenwoodSentinel();
        Card firstLand = new Plains();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(creature, firstLand, secondLand));
        addActivationMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        harness.castFromExile(player1, firstLand.getId());
        harness.assertOnBattlefield(player1, "Plains");
        assertThatThrownBy(() -> harness.castFromExile(player1, secondLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secondLand);
    }

    @Test
    void shortLibraryExilesOnlyAvailableCardsAndPermissionExpires() {
        harness.addToBattlefield(player1, new GolosTirelessPilgrim());
        Card creature = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(creature));
        addActivationMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(creature.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(creature.getId());
    }

    @Test
    void freeCreatureStillRequiresNormalTiming() {
        harness.addToBattlefield(player1, new GolosTirelessPilgrim());
        Card creature = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(creature));
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        addActivationMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
