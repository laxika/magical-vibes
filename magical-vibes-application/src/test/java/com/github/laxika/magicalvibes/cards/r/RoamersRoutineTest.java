package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoamersRoutine.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class RoamersRoutineTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers basic lands to enter the battlefield tapped")
    void resolvesBasicLandSearch() {
        harness.setHand(player1, List.of(new RoamersRoutine()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
        harness.assertInGraveyard(player1, "Roamer's Routine");
    }

    @Test
    @DisplayName("Harmonize taps a creature to reduce the cost and exiles the spell")
    void harmonizeSearchesAndExilesSpell() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.addToBattlefield(player1, creature);
        harness.setGraveyard(player1, List.of(new RoamersRoutine()));
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashbackWithTapCost(player1, 0,
                List.of(harness.getPermanentId(player1, "Grizzly Bears")));
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature && permanent.isTapped());
        harness.assertNotInGraveyard(player1, "Roamer's Routine");
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Roamer's Routine"));
    }
}
