package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BalefireDragon;
import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.b.BroodmateDragon;
import com.github.laxika.magicalvibes.cards.g.GoldspanDragon;
import com.github.laxika.magicalvibes.cards.g.GatherSpecimens;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningDragon;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tiamat.class, BalefireDragon.class, BeaconOfUnrest.class, BroodmateDragon.class,
        GoldspanDragon.class, GatherSpecimens.class, GrizzlyBears.class, LightningDragon.class, ShivanDragon.class})
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
        Tiamat target = new Tiamat();
        harness.setGraveyard(player1, List.of(target));
        setLibrary(new ShivanDragon());
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tiamat");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .contains("Shivan Dragon");
    }

    @Test
    @DisplayName("The search may be declined even when eligible Dragons exist")
    void mayChooseNoDragons() {
        setLibrary(new ShivanDragon(), new BroodmateDragon());
        castTiamat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shivan Dragon", "Broodmate Dragon");
    }

    @Test
    @DisplayName("The search may stop after fewer than five Dragons")
    void mayStopAfterOneDragon() {
        setLibrary(new ShivanDragon(), new ShivanDragon(), new BroodmateDragon());
        castTiamat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        chooseCard("Shivan Dragon");
        assertThat(activeSearch().params().cards()).extracting(Card::getName)
                .containsExactly("Broodmate Dragon");
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Shivan Dragon");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shivan Dragon", "Broodmate Dragon");
    }

    @Test
    @DisplayName("The search finishes when only duplicate or excluded cards remain")
    void searchFinishesWhenNoDistinctDragonsRemain() {
        setLibrary(new ShivanDragon(), new ShivanDragon(), new Tiamat(), new GrizzlyBears());
        castTiamat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        chooseCard("Shivan Dragon");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Shivan Dragon");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shivan Dragon", "Tiamat", "Grizzly Bears");
    }

    @Test
    @DisplayName("Tiamat entering under a different player's control does not trigger for that player")
    void opponentWhoDidNotCastTiamatDoesNotSearch() {
        harness.setLibrary(player2, List.of(new BroodmateDragon()));
        harness.setHand(player2, List.of(new GatherSpecimens()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        castTiamat();

        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tiamat");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Broodmate Dragon");
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
        harness.setLibrary(player1, List.of(cards));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseCard(String cardName) {
        List<String> offeredNames = activeSearch().params().cards().stream()
                .map(Card::getName)
                .toList();
        int index = offeredNames.indexOf(cardName);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
