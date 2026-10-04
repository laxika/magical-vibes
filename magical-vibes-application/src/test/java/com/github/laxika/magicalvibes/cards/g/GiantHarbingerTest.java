package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.c.CrushUnderfoot;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantHarbinger.class, BlindSpotGiant.class, HillGiant.class, GrizzlyBears.class,
        Island.class, WoodlandChangeling.class, CrushUnderfoot.class})
class GiantHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Giant Harbinger creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Giant Harbinger");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability only offers Giant cards")
    void acceptingMayOffersOnlyGiants() {
        setupAndCast();
        setupLibraryWithGiants();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .isNotEmpty()
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.GIANT));
    }

    @Test
    @DisplayName("Choosing a Giant card puts it on top of the library")
    void choosingGiantPutsItOnTop() {
        setupAndCast();
        setupLibraryWithGiants();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).isNotEmpty();
        assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithGiants();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GiantHarbinger()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
    }

    private void setupLibraryWithGiants() {
        harness.setLibrary(player1, List.of(new HillGiant(), new BlindSpotGiant(), new GrizzlyBears(), new Island()));
    }

    @Test
    @DisplayName("The search can find a changeling in the library")
    void canFindChangeling() {
        Card changeling = new WoodlandChangeling();
        setupAndCast();
        harness.setLibrary(player1, List.of(new Island(), changeling));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(changeling);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(changeling);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A noncreature Giant card can be revealed and put on top")
    void canFindKindredGiant() {
        Card giant = new CrushUnderfoot();
        Card island = new Island();
        setupAndCast();
        harness.setLibrary(player1, List.of(island, giant));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(giant);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(giant, island);
        assertThat(gd.gameLog).anySatisfy(entry -> assertThat(entry.plainText())
                .contains("reveals Crush Underfoot"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may fail to find even with a Giant available")
    void canFailToFind() {
        Card giant = new BlindSpotGiant();
        Card island = new Island();
        setupAndCast();
        harness.setLibrary(player1, List.of(giant, island));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(giant, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).anySatisfy(entry -> assertThat(entry.plainText()).contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Accepting a search with no Giants finishes without a card choice")
    void noGiantsFinishesSearch() {
        Card island = new Island();
        setupAndCast();
        harness.setLibrary(player1, List.of(island));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).anySatisfy(entry -> assertThat(entry.plainText()).contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Accepting a search of an empty library finishes normally")
    void emptyLibraryFinishesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining leaves the library in its original order")
    void decliningLeavesLibraryUnchanged() {
        Card giant = new BlindSpotGiant();
        Card island = new Island();
        setupAndCast();
        harness.setLibrary(player1, List.of(island, giant));

        harness.passBothPriorities();
        harness.passBothPriorities();
        GameData gd = harness.getGameData();
        int logSize = gd.gameLog.size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, giant);
        assertThat(gd.gameLog.subList(logSize, gd.gameLog.size()))
                .noneSatisfy(entry -> assertThat(entry.plainText()).containsIgnoringCase("shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
