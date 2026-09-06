package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ExplosiveVegetation.class, Forest.class, Plains.class, GrizzlyBears.class})
class ExplosiveVegetationTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers up to two basic lands for the battlefield tapped")
    void offersBasicLandsForBattlefieldTapped() {
        castExplosiveVegetation();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing two basic lands puts both onto the battlefield tapped")
    void chosenBasicLandsEnterTapped() {
        castExplosiveVegetation();

        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        int battlefieldBefore = gameData.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(gameData, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gameData.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find any basic lands")
    void canFailToFind() {
        castExplosiveVegetation();

        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        int battlefieldBefore = gameData.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(gameData, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gameData.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    private void castExplosiveVegetation() {
        harness.setHand(player1, List.of(new ExplosiveVegetation()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new GrizzlyBears()));
    }
}
