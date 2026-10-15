package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PrimalHuntbeast;
import com.github.laxika.magicalvibes.cards.s.SentinelSpider;
import com.github.laxika.magicalvibes.cards.s.SpikedBaloth;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.cards.v.VastwoodGorger;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MwonvuliBeastTracker.class, SentinelSpider.class, SpikedBaloth.class,
        VastwoodGorger.class, Island.class, VampireNighthawk.class, PrimalHuntbeast.class})
class MwonvuliBeastTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("The enter trigger only offers creature cards with deathtouch, hexproof, reach or trample")
    void searchOffersOnlyMatchingCreatures() {
        setupLibrary();
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Mwonvuli Beast Tracker");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Sentinel Spider", "Spiked Baloth");
    }

    @Test
    @DisplayName("The search may fail to find")
    void searchMayFailToFind() {
        setupLibrary();
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind())
                .isTrue();
        List<Card> originalCards = List.copyOf(gd.playerDecks.get(player1.getId()));
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(originalCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The chosen creature card is put on top of the library")
    void chosenCardGoesOnTop() {
        setupLibrary();
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo(chosenName);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new MwonvuliBeastTracker()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new SentinelSpider(), new SpikedBaloth(),
                new VastwoodGorger(), new Island()));
    }

    @Test
    @DisplayName("Deathtouch without reach and hexproof creatures are eligible")
    void offersDeathtouchAndHexproofCreatures() {
        harness.setLibrary(player1, List.of(new VampireNighthawk(), new PrimalHuntbeast(),
                new VastwoodGorger(), new Island()));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Vampire Nighthawk", "Primal Huntbeast");
    }

    @Test
    @DisplayName("A library with no eligible creature finishes the trigger without a choice")
    void noMatchingCreature() {
        Card creature = new VastwoodGorger();
        Card land = new Island();
        harness.setLibrary(player1, List.of(creature, land));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, land);
    }

    @Test
    @DisplayName("An empty library finishes the trigger without a choice")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
    }
}
