package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BalefireDragon;
import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.b.BroodmateDragon;
import com.github.laxika.magicalvibes.cards.g.GoldspanDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningDragon;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tiamat.class, BalefireDragon.class, BeaconOfUnrest.class, BroodmateDragon.class,
        GoldspanDragon.class, GrizzlyBears.class, LightningDragon.class, ShivanDragon.class})
class TiamatTest extends BaseCardTest {

    @Test
    @DisplayName("Cast ETB searches for up to five differently named Dragons other than Tiamat")
    void castEtbSearchesForDistinctDragons() {
        setLibrary(new Tiamat(), new GrizzlyBears(), new ShivanDragon(), new ShivanDragon(),
                new GoldspanDragon(), new BalefireDragon(), new BroodmateDragon(), new LightningDragon());
        castTiamat();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shivan Dragon", "Shivan Dragon", "Goldspan Dragon",
                        "Balefire Dragon", "Broodmate Dragon", "Lightning Dragon");
        assertThat(search.params().remainingCount()).isEqualTo(5);

        chooseCard("Shivan Dragon");
        chooseCard("Goldspan Dragon");
        chooseCard("Balefire Dragon");
        chooseCard("Broodmate Dragon");
        chooseCard("Lightning Dragon");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shivan Dragon", "Goldspan Dragon", "Balefire Dragon",
                        "Broodmate Dragon", "Lightning Dragon");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Tiamat", "Grizzly Bears", "Shivan Dragon");
    }

    @Test
    @DisplayName("An uncast Tiamat entering the battlefield does not search")
    void uncastTiamatDoesNotSearch() {
        harness.setGraveyard(player1, List.of(new Tiamat()));
        setLibrary(new ShivanDragon());
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tiamat");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .contains("Shivan Dragon");
    }

    private void castTiamat() {
        harness.setHand(player1, List.of(new Tiamat()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        GameData gameData = harness.getGameData();
        return gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseCard(String cardName) {
        List<String> offeredNames = activeSearch().params().cards().stream()
                .map(Card::getName)
                .toList();
        int index = offeredNames.indexOf(cardName);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
