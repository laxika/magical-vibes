package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AzoriusChancery;
import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VigeanIntuition.class, AzoriusChancery.class, AzoriusFirstWing.class, VisionSkeins.class})
class VigeanIntuitionTest extends BaseCardTest {

    @Test
    void putsCardsOfChosenTypeIntoHandAndTheRestIntoGraveyard() {
        Card firstLand = new AzoriusChancery();
        Card secondLand = new AzoriusChancery();
        Card creature = new AzoriusFirstWing();
        Card instant = new VisionSkeins();
        harness.setLibrary(player1, List.of(firstLand, secondLand, creature, instant));

        harness.castFromHand(player1, new VigeanIntuition(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, CardType.LAND.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void processesTheAvailableCardsWhenLibraryHasFewerThanFour() {
        Card land = new AzoriusChancery();
        Card creature = new AzoriusFirstWing();
        Card instant = new VisionSkeins();
        harness.setLibrary(player1, List.of(land, creature, instant));

        harness.castFromHand(player1, new VigeanIntuition(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
