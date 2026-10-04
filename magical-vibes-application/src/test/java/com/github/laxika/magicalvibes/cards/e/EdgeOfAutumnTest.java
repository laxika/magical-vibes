package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EdgeOfAutumn.class, Forest.class, Plains.class, HorizonCanopy.class, Imperiosaur.class, Stabilizer.class})
class EdgeOfAutumnTest extends BaseCardTest {

    @Test
    @DisplayName("Casting searches for a tapped basic land when controlling four or fewer lands")
    void castingSearchesForTappedBasicLandWithFourOrFewerLands() {
        addForests(4);
        Card basicLand = new Plains();
        Card nonBasicLand = new HorizonCanopy();
        Card nonLand = new Imperiosaur();
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(basicLand, nonBasicLand, nonLand));
        addSpellMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(basicLand);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == basicLand && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonBasicLand, nonLand);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Edge of Autumn");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Casting does not search when controlling more than four lands")
    void castingSkipsSearchWithMoreThanFourLands() {
        addForests(5);
        Card basicLand = new Plains();
        Card nonBasicLand = new HorizonCanopy();
        Card nonLand = new Imperiosaur();
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(basicLand, nonBasicLand, nonLand));
        addSpellMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(basicLand, nonBasicLand, nonLand);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Edge of Autumn");
    }

    @Test
    @DisplayName("Cycling sacrifices a land and draws without resolving the sorcery effect")
    void cyclingSacrificesLandAndDraws() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new Forest());
        Card drawnCard = new Imperiosaur();
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(new Plains(), drawnCard));
        addSpellMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Plains"));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
        harness.assertInGraveyard(player1, "Edge of Autumn");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Cycling prompts when more than one land can be sacrificed")
    void cyclingPromptsForSacrificeChoice() {
        List<Permanent> lands = addForests(5);
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(new Imperiosaur()));

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, lands.getFirst().getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Edge of Autumn");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Imperiosaur");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Cycling draws even when five lands remain after the sacrifice")
    void cyclingDrawsWhenFiveLandsRemainAfterSacrifice() {
        List<Permanent> lands = addForests(6);
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(new Imperiosaur()));

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, lands.getFirst().getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Edge of Autumn");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Imperiosaur");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Cycling cannot be activated without a land to sacrifice")
    void cyclingRequiresLandToSacrifice() {
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        addSpellMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("The land-count condition is checked on resolution")
    void gainingFifthLandBeforeResolutionPreventsSearch() {
        addForests(4);
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(new Plains()));
        addSpellMana();

        harness.castSorcery(player1, 0);
        harness.addToBattlefield(player1, new HorizonCanopy());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Edge of Autumn");
    }

    @Test
    @DisplayName("A restricted basic-land search may fail to find")
    void searchMayFailToFindAnAvailableBasicLand() {
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(new Plains()));
        addSpellMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Edge of Autumn");
    }

    @Test
    @DisplayName("Stabilizer prevents sacrifice-cost cycling before costs are paid")
    void stabilizerPreventsCycling() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Stabilizer());
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(new Imperiosaur()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't cycle");

        harness.assertInHand(player1, "Edge of Autumn");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.stack).isEmpty();
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private List<Permanent> addForests(int count) {
        List<Permanent> lands = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            lands.add(harness.addToBattlefieldAndReturn(player1, new Forest()));
        }
        return lands;
    }
}
