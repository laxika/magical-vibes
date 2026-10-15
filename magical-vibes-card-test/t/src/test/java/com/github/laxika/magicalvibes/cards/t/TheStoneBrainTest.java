package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheStoneBrain.class, Forest.class, GrizzlyBears.class, LightningBolt.class})
class TheStoneBrainTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to four named cards and the opponent draws for hand cards exiled")
    void exilesUpToFourAndDrawsForHandExiles() {
        List<Card> hand = new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));
        List<Card> graveyard = new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears()));
        List<Card> library = new ArrayList<>(List.of(new GrizzlyBears(), new Forest(), new LightningBolt()));
        harness.setHand(player2, hand);
        harness.setGraveyard(player2, graveyard);
        harness.setLibrary(player2, library);
        harness.addToBattlefield(player1, new TheStoneBrain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        PendingInteraction.MultiZoneExileChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(4);

        harness.handleMultipleCardsChosen(player1, List.of(
                hand.get(0).getId(), hand.get(1).getId(), graveyard.get(0).getId(), graveyard.get(1).getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsOnly("Grizzly Bears")
                .hasSize(4);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .contains("Lightning Bolt")
                .hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("The Stone Brain");
    }

    @Test
    @DisplayName("Can exile basic lands from library without drawing and leaves battlefield copies alone")
    void exilesBasicLandsFromLibraryWithoutDrawing() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(first, second, new Forest()));
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new TheStoneBrain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "The Stone Brain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Forest");
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May choose zero matching cards even when matching cards are available")
    void mayExileZeroCards() {
        Forest handCard = new Forest();
        Forest libraryCard = new Forest();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addToBattlefield(player1, new TheStoneBrain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Forest");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate outside a main phase")
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new TheStoneBrain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "The Stone Brain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new TheStoneBrain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(RuntimeException.class);
        harness.assertOnBattlefield(player1, "The Stone Brain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
