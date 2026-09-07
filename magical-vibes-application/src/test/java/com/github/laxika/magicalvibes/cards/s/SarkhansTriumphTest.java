package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({SarkhansTriumph.class, ShivanDragon.class, GrizzlyBears.class})
class SarkhansTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only Dragon creature cards")
    void offersOnlyDragonCreatures() {
        ShivanDragon dragon = new ShivanDragon();
        setUpAndCast(dragon, new GrizzlyBears());

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactly(dragon)
                .allMatch(card -> card.hasType(CardType.CREATURE)
                        && card.getSubtypes().contains(CardSubtype.DRAGON));
    }

    @Test
    @DisplayName("Choosing a Dragon puts it into hand and shuffles the library")
    void choosingDragonPutsItIntoHand() {
        ShivanDragon dragon = new ShivanDragon();
        GrizzlyBears nonDragon = new GrizzlyBears();
        setUpAndCast(dragon, nonDragon);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDragon);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library without a Dragon resolves without a search interaction")
    void noDragonFound() {
        GrizzlyBears nonDragon = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonDragon));
        harness.setHand(player1, List.of(new SarkhansTriumph()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDragon);
    }

    private void setUpAndCast(Card... libraryCards) {
        harness.setLibrary(player1, List.of(libraryCards));
        harness.setHand(player1, List.of(new SarkhansTriumph()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
