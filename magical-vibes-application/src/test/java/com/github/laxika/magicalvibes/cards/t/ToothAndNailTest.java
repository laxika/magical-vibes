package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToothAndNail.class, AlphaMyr.class, CopperMyr.class, Forest.class, SoulWarden.class})
class ToothAndNailTest extends BaseCardTest {

    @Test
    @DisplayName("Search mode puts up to two creature cards into your hand")
    void searchesForCreatures() {
        cast(new int[]{0}, false, List.of(new ToothAndNail()),
                List.of(new AlphaMyr(), new CopperMyr(), new Forest()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Alpha Myr");
        harness.assertInHand(player1, "Copper Myr");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Search mode can find fewer than two creature cards")
    void searchesForOnlyOneAvailableCreature() {
        cast(new int[]{0}, false, List.of(new ToothAndNail()),
                List.of(new AlphaMyr(), new Forest()));

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Alpha Myr");
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .extracting(Card::getName)
                .isEqualTo("Forest");
    }

    @Test
    @DisplayName("Search mode may find zero creature cards")
    void mayDeclineSearchingForCreatures() {
        cast(new int[]{0}, false, List.of(new ToothAndNail()),
                List.of(new AlphaMyr(), new Forest()));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Battlefield mode puts up to two creature cards from hand onto the battlefield")
    void putsCreaturesFromHand() {
        cast(new int[]{1}, false,
                List.of(new ToothAndNail(), new AlphaMyr(), new CopperMyr()), List.of());

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertNotOnBattlefield(player1, "Copper Myr");
        harness.assertInHand(player1, "Copper Myr");
    }

    @Test
    @DisplayName("Battlefield mode may put zero creatures from hand onto the battlefield")
    void mayDeclinePuttingCreaturesFromHand() {
        cast(new int[]{1}, false,
                List.of(new ToothAndNail(), new AlphaMyr(), new CopperMyr()), List.of());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Alpha Myr");
        harness.assertNotOnBattlefield(player1, "Copper Myr");
        harness.assertInHand(player1, "Alpha Myr");
        harness.assertInHand(player1, "Copper Myr");
        harness.assertInGraveyard(player1, "Tooth and Nail");
    }

    @Test
    @DisplayName("Creatures put onto the battlefield together see each other enter")
    void creaturesEnterSimultaneously() {
        harness.setLife(player1, 20);
        cast(new int[]{1}, false,
                List.of(new ToothAndNail(), new AlphaMyr(), new SoulWarden()), List.of());

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertOnBattlefield(player1, "Soul Warden");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Search mode can stop after one creature even when another is available")
    void searchCanStopAfterOneCreature() {
        cast(new int[]{0}, false, List.of(new ToothAndNail()),
                List.of(new AlphaMyr(), new CopperMyr()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Alpha Myr");
        harness.assertNotInHand(player1, "Copper Myr");
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .extracting(Card::getName).isEqualTo("Copper Myr");
        harness.assertInGraveyard(player1, "Tooth and Nail");
    }

    @Test
    @DisplayName("Battlefield mode cannot put a noncreature card onto the battlefield")
    void cannotPutNoncreatureFromHand() {
        cast(new int[]{1}, false,
                List.of(new ToothAndNail(), new Forest(), new AlphaMyr()), List.of());

        harness.handleMayAbilityChosen(player1, true);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Tooth and Nail");
    }

    @Test
    @DisplayName("Entwine resolves both modes")
    void entwinesBothModes() {
        cast(new int[]{0, 1}, true, List.of(new ToothAndNail()),
                List.of(new AlphaMyr(), new CopperMyr()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertOnBattlefield(player1, "Copper Myr");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Entwine may put creatures already in hand onto the battlefield instead of searched cards")
    void entwineCanPutDifferentCreaturesFromHand() {
        cast(new int[]{1, 0}, true,
                List.of(new ToothAndNail(), new CopperMyr()), List.of(new AlphaMyr()));

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Copper Myr");
        harness.assertNotOnBattlefield(player1, "Alpha Myr");
        harness.assertInHand(player1, "Alpha Myr");
        harness.assertInGraveyard(player1, "Tooth and Nail");
    }

    @Test
    @DisplayName("Entwine requires its additional two generic mana")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new ToothAndNail()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, boolean entwined, List<Card> hand, List<Card> library) {
        harness.setHand(player1, hand);
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 7 : 5);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }

}
