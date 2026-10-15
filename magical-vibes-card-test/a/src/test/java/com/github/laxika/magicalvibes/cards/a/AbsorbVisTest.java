package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbsorbVis.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class AbsorbVisTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses 4 life and controller gains 4 life")
    void drainsFourAndGainsFour() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AbsorbVis()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Absorb Vis cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new AbsorbVis()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Basic landcycling discards the card and offers only basic lands")
    void basicLandcyclingDiscardsAndSearches() {
        harness.setHand(player1, List.of(new AbsorbVis()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Absorb Vis");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(3);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(),
                new GrizzlyBears()));
    }

    @Test
    void canTargetSelfWithoutChangingFinalLifeTotal() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new AbsorbVis()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 3);
        harness.assertInGraveyard(player1, "Absorb Vis");
    }

    @Test
    void basicLandcyclingPutsSelectedLandIntoHandWithoutDrainingLife() {
        harness.setHand(player1, List.of(new AbsorbVis()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Absorb Vis");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Plains"));
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void basicLandcyclingCanFailToFindEvenWithBasicLandsAvailable() {
        harness.setHand(player1, List.of(new AbsorbVis()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Absorb Vis");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void basicLandcyclingResolvesWithAnEmptyLibrary() {
        harness.setHand(player1, List.of(new AbsorbVis()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Absorb Vis");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void basicLandcyclingRequiresBlackManaAndDoesNotDiscardOnFailedPayment() {
        harness.setHand(player1, List.of(new AbsorbVis()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Absorb Vis");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
