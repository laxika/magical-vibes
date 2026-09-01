package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Renewal.class, Forest.class, GrizzlyBears.class, Plains.class})
class RenewalTest extends BaseCardTest {

    private void castRenewalSacrificingLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Renewal()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID landId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castSorceryWithSacrifice(player1, 0, landId);
    }

    @Test
    @DisplayName("Casting sacrifices a land as an additional cost")
    void castingSacrificesLand() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        castRenewalSacrificingLand();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting cannot sacrifice a nonland permanent")
    void cannotSacrificeNonland() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.setHand(player1, List.of(new Renewal()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice");
    }

    @Test
    @DisplayName("Resolving offers only basic land cards from the library")
    void resolvingOffersOnlyBasicLands() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        castRenewalSacrificingLand();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName)).containsExactly("Plains");
    }

    @Test
    @DisplayName("Choosing a basic land puts it onto the battlefield untapped and schedules a draw")
    void chosenLandEntersUntappedAndDrawIsScheduled() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        castRenewalSacrificingLand();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getName()).isEqualTo("Plains");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The basic land search may be declined")
    void canFailToFindBasicLand() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        castRenewalSacrificingLand();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        castRenewalSacrificingLand();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
