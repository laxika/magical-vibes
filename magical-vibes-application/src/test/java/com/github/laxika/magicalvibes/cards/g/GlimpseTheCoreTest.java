package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CavernousMaw;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimpseTheCore.class, Forest.class, GrizzlyBears.class, CavernousMaw.class, Plains.class})
class GlimpseTheCoreTest extends BaseCardTest {

    @Test
    @DisplayName("Forest mode puts a basic Forest from the library onto the battlefield tapped")
    void forestModePutsBasicForestOntoBattlefieldTapped() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        cast(0, null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Forest");

        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
        assertThat(gameData.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Cave mode returns a targeted Cave card from the graveyard tapped")
    void caveModeReturnsTargetedCaveTapped() {
        Card cave = createCave();
        harness.setGraveyard(player1, List.of(cave));
        cast(1, cave.getId());

        Permanent returnedCave = findPermanent(player1, "Test Cave");
        assertThat(returnedCave.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Test Cave");
    }

    @Test
    @DisplayName("Cave mode cannot target a non-Cave card")
    void caveModeRejectsNonCaveCard() {
        Card nonCave = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCave));
        harness.setHand(player1, List.of(new GlimpseTheCore()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, nonCave.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forest mode offers only a basic Forest among other basic and nonbasic lands")
    void forestModeFiltersOtherLands() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        CavernousMaw cave = new CavernousMaw();
        harness.setLibrary(player1, List.of(plains, cave, forest));
        cast(0, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, cave);
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Cavernous Maw");
    }

    @Test
    @DisplayName("Forest mode may fail to find even when a Forest is available")
    void forestModeMayFailToFind() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        cast(0, null);

        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Glimpse the Core");
    }

    @Test
    @DisplayName("Forest mode resolves with an empty library and no Cave target")
    void forestModeWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        cast(0, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Glimpse the Core");
    }

    @Test
    @DisplayName("Forest mode cannot find a Cave land without the Forest subtype")
    void forestModeDoesNotFindCave() {
        CavernousMaw cave = new CavernousMaw();
        harness.setLibrary(player1, List.of(cave));
        cast(0, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cave);
        harness.assertNotOnBattlefield(player1, "Cavernous Maw");
    }

    @Test
    @DisplayName("Cave mode returns only the chosen Cave and does not search the library")
    void caveModeReturnsOnlyChosenCave() {
        CavernousMaw chosen = new CavernousMaw();
        CavernousMaw other = new CavernousMaw();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(chosen, other));
        harness.setLibrary(player1, List.of(forest));
        cast(1, chosen.getId());

        Permanent returned = findPermanent(player1, "Cavernous Maw");
        assertThat(returned.getCard().getId()).isEqualTo(chosen.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cave mode cannot target an opponent's Cave")
    void caveModeRejectsOpponentsCave() {
        CavernousMaw cave = new CavernousMaw();
        harness.setGraveyard(player2, List.of(cave));
        harness.setHand(player1, List.of(new GlimpseTheCore()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, cave.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cave mode requires a target")
    void caveModeRequiresTarget() {
        harness.setGraveyard(player1, List.of(new CavernousMaw()));
        harness.setHand(player1, List.of(new GlimpseTheCore()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cave mode does not return another Cave when its target leaves the graveyard")
    void caveModeDoesNotRetarget() {
        CavernousMaw target = new CavernousMaw();
        CavernousMaw other = new CavernousMaw();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new GlimpseTheCore()));
        addMana();
        harness.castSorcery(player1, 0, 1, target.getId());

        harness.setGraveyard(player1, List.of(other));
        harness.setHand(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cavernous Maw");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
        harness.assertInHand(player1, "Cavernous Maw");
        harness.assertInGraveyard(player1, "Glimpse the Core");
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new GlimpseTheCore()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, mode, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Card createCave() {
        Card cave = new Card();
        cave.setName("Test Cave");
        cave.setType(CardType.LAND);
        cave.setSubtypes(List.of(CardSubtype.CAVE));
        return cave;
    }
}
