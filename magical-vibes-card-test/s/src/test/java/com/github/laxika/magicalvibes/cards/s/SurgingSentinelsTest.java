package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurgingSentinels.class, SnowCoveredForest.class, SnowCoveredMountain.class})
class SurgingSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("Ripple offers every revealed card with the same name")
    void rippleOffersEverySameNameCard() {
        prepareCaster(List.of(
                new SnowCoveredMountain(),
                new SurgingSentinels(),
                new SnowCoveredForest(),
                new SurgingSentinels()));

        castSentinels();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL
                && entry.getCard().getName().equals("Surging Sentinels"));
    }

    @Test
    @DisplayName("Ripple casts a matching revealed creature without paying its mana cost")
    void rippleCastsMatchingCreatureWithoutPayingMana() {
        prepareCaster(List.of(new SurgingSentinels()));

        castSentinels();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Surging Sentinels")).isEqualTo(2);
    }

    @Test
    @DisplayName("Ripple lets the player decline and order all revealed cards on the bottom")
    void declineOrdersRevealedCardsOnBottom() {
        prepareCaster(List.of(
                new SnowCoveredMountain(),
                new SnowCoveredForest(),
                new SnowCoveredMountain(),
                new SnowCoveredForest()));

        castSentinels();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).hasSize(4);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Snow-Covered Forest", "Snow-Covered Mountain",
                        "Snow-Covered Forest", "Snow-Covered Mountain");
    }

    @Test
    @DisplayName("Ripple can be declined without revealing cards")
    void rippleCanBeDeclined() {
        List<Card> libraryTop = List.of(new SnowCoveredMountain(), new SnowCoveredForest());
        prepareCaster(libraryTop);

        castSentinels();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Snow-Covered Mountain", "Snow-Covered Forest");
    }

    @Test
    @DisplayName("The reveal choice is made when the ripple trigger resolves")
    void revealChoiceWaitsForTriggerResolution() {
        prepareCaster(List.of(new SnowCoveredMountain()));

        castSentinels();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Surging Sentinels");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ripple reveals only four cards and leaves a fifth matching card on top")
    void rippleDoesNotRevealFifthCard() {
        SurgingSentinels fifthCard = new SurgingSentinels();
        prepareCaster(List.of(new SnowCoveredMountain(), new SnowCoveredForest(),
                new SnowCoveredMountain(), new SnowCoveredForest(), fifthCard));

        castSentinels();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifthCard);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Surging Sentinels")).isEqualTo(1);
    }

    @Test
    @DisplayName("A matching revealed card may be declined and goes to the bottom")
    void declinedMatchingCardGoesToBottom() {
        SurgingSentinels revealedCard = new SurgingSentinels();
        prepareCaster(List.of(revealedCard));

        castSentinels();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealedCard);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Surging Sentinels")).isEqualTo(1);
    }

    @Test
    @DisplayName("Ripple with an empty library leaves the original creature spell able to resolve")
    void rippleWithEmptyLibrary() {
        prepareCaster(List.of());

        castSentinels();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Surging Sentinels");
    }

    private void prepareCaster(List<Card> libraryTop) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, libraryTop);
    }

    private void castSentinels() {
        harness.castFromHand(player1, new SurgingSentinels(), "{2}{W}");
    }
}
