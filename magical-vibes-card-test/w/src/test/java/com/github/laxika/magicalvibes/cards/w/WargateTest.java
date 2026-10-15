package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wargate.class, AirElemental.class, GrizzlyBears.class, Ornithopter.class, Plains.class,
        Shock.class, MindStone.class, GloriousAnthem.class})
class WargateTest extends BaseCardTest {

    @Test
    @DisplayName("Presents permanents of any type with MV <= X, excluding higher-MV and non-permanent cards")
    void presentsPermanentsWithinX() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Library: GrizzlyBears (creature MV2), Ornithopter (artifact MV0), Plains (land MV0),
        // AirElemental (creature MV5), Shock (instant MV1).
        // X=2 → permanents with MV <= 2: GrizzlyBears, Ornithopter, Plains.
        assertThat(offeredNames(gd)).containsExactlyInAnyOrder("Grizzly Bears", "Ornithopter", "Plains");
    }

    @Test
    @DisplayName("Excludes permanents with MV greater than X")
    void excludesPermanentsAboveX() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(offeredNames(gd)).doesNotContain("Air Elemental");
    }

    @Test
    @DisplayName("Excludes non-permanent cards even when their mana value is within X")
    void excludesNonPermanentCards() {
        castWargate(10);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Shock (instant, MV1) is within X=10 but is not a permanent card.
        assertThat(offeredNames(gd)).doesNotContain("Shock");
        assertThat(offeredNames(gd)).containsExactlyInAnyOrder("Grizzly Bears", "Ornithopter", "Plains", "Air Elemental");
    }

    @Test
    @DisplayName("Search destination is the battlefield")
    void destinationIsBattlefield() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Choosing a permanent puts it onto the battlefield")
    void choosingPermanentPutsItOntoBattlefield() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals(chosenName));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find, leaving the battlefield unchanged")
    void failToFind() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        int battlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Wargate is put into the graveyard after resolving, not shuffled into the library")
    void wargateGoesToGraveyard() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Wargate");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Wargate"));
    }

    @Test
    @DisplayName("X zero can find a land and puts it onto the battlefield untapped")
    void zeroXCanFindLand() {
        castWargate(0);
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(land, new GrizzlyBears(), new Shock()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(offeredNames(gd)).containsExactly("Plains");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> {
                    assertThat(p.getCard().getId()).isEqualTo(land.getId());
                    assertThat(p.isTapped()).isFalse();
                });
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2)
                .noneMatch(c -> c.getId().equals(land.getId()));
        harness.assertInGraveyard(player1, "Wargate");
    }

    @Test
    @DisplayName("X zero can find a zero-mana artifact creature")
    void zeroXCanFindArtifactCreature() {
        castWargate(0);
        harness.setLibrary(player1, List.of(new Ornithopter(), new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(offeredNames(harness.getGameData())).containsExactly("Ornithopter");
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Wargate");
    }

    @Test
    @DisplayName("A noncreature artifact at the X bound can enter the battlefield")
    void canFindNoncreatureArtifact() {
        castWargate(2);
        harness.setLibrary(player1, List.of(new MindStone(), new GloriousAnthem()));

        harness.passBothPriorities();

        assertThat(offeredNames(harness.getGameData())).containsExactly("Mind Stone");
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Wargate");
    }

    @Test
    @DisplayName("An enchantment at the X bound can enter the battlefield")
    void canFindEnchantment() {
        castWargate(3);
        harness.setLibrary(player1, List.of(new GloriousAnthem()));

        harness.passBothPriorities();

        assertThat(offeredNames(harness.getGameData())).containsExactly("Glorious Anthem");
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Wargate");
    }

    @Test
    @DisplayName("An empty library does not leave the spell waiting for a choice")
    void emptyLibraryResolves() {
        castWargate(2);
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Wargate");
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A library without an eligible permanent is left intact")
    void noEligiblePermanentResolves() {
        castWargate(2);
        AirElemental creature = new AirElemental();
        Shock instant = new Shock();
        harness.setLibrary(player1, List.of(creature, instant));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, instant);
        harness.assertInGraveyard(player1, "Wargate");
    }

    @Test
    @DisplayName("Failing to find still shuffles and completes resolution without removing library cards")
    void failToFindStillShufflesAndCompletesResolution() {
        castWargate(2);
        GrizzlyBears creature = new GrizzlyBears();
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(creature, land));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, land);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Wargate");
    }

    @Test
    @DisplayName("Searching only moves a card from the controller's library and then shuffles")
    void searchUsesOnlyControllersLibrary() {
        castWargate(2);
        GrizzlyBears ownCard = new GrizzlyBears();
        MindStone opposingCard = new MindStone();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opposingCard));

        harness.passBothPriorities();

        assertThat(offeredNames(harness.getGameData())).containsExactly("Grizzly Bears");
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player2.getId())).containsExactly(opposingCard);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        harness.assertInGraveyard(player1, "Wargate");
    }

    private void castWargate(int xValue) {
        harness.setHand(player1, List.of(new Wargate()));
        // {X}{G}{W}{U}: X generic paid with the extra green.
        harness.addMana(player1, ManaColor.GREEN, xValue + 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, xValue);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Ornithopter(), new Plains(), new AirElemental(), new Shock()));
    }

    private List<String> offeredNames(GameData gd) {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
    }
}
