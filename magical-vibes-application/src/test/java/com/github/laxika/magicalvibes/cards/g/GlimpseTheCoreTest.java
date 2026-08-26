package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimpseTheCore.class, Forest.class, GrizzlyBears.class})
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

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(0));

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

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new GlimpseTheCore()));
        addMana();
        harness.castSorcery(player1, 0, mode, targetId);
        harness.passBothPriorities();
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
