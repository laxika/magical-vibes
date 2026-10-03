package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ApexOfPower.class, Divination.class, Island.class})
class ApexOfPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles seven cards, permits nonlands to be cast, and adds ten chosen-color mana from hand")
    void exilesSevenAndAddsManaWhenCastFromHand() {
        Card land = new Island();
        List<Card> topCards = List.of(
                new Divination(), land, new Divination(),
                new Divination(), new Divination(),
                new Divination(), new Divination());
        harness.setLibrary(player1, topCards);
        harness.setHand(player1, List.of(new ApexOfPower()));
        addApexMana(player1);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(topCards.stream().map(Card::getId).toList());
        assertThat(gd.exilePlayPermissions).containsKeys(
                topCards.get(0).getId(), topCards.get(2).getId(), topCards.get(3).getId(),
                topCards.get(4).getId(), topCards.get(5).getId(), topCards.get(6).getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(10);
    }

    @Test
    @DisplayName("Does not add mana when cast from exile")
    void doesNotAddManaWhenCastFromExile() {
        ApexOfPower apex = new ApexOfPower();
        harness.setExile(player1, List.of(apex));
        gd.exilePlayPermissions.put(apex.getId(), player1.getId());
        harness.setLibrary(player1, List.of(
                new Divination(), new Divination(), new Divination(),
                new Divination(), new Divination(), new Divination(),
                new Divination()));
        addApexMana(player1);

        harness.castFromExile(player1, apex.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private void addApexMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 7);
        harness.addMana(player, ManaColor.RED, 3);
    }

    @Test
    @DisplayName("Exiled spells require their normal mana cost and can use the ten mana")
    void castsExiledSpellWithAwardedMana() {
        Divination divination = new Divination();
        List<Card> topCards = List.of(divination, new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island());
        Island firstDraw = new Island();
        Island secondDraw = new Island();
        harness.setLibrary(player1, java.util.stream.Stream.concat(topCards.stream(),
                java.util.stream.Stream.of(firstDraw, secondDraw)).toList());
        harness.setHand(player1, List.of(new ApexOfPower()));
        addApexMana(player1);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleListChoice(player1, "BLUE");
        harness.castFromExile(player1, divination.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(7);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(divination);
    }

    @Test
    @DisplayName("A short library is fully exiled and casting permission does not allow lands or free spells")
    void shortLibraryDoesNotGrantFreeCastingOrLandPlays() {
        Divination divination = new Divination();
        Island island = new Island();
        harness.setLibrary(player1, List.of(divination, island));
        harness.setHand(player1, List.of(new ApexOfPower()));
        addApexMana(player1);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(divination, island);
        assertThatThrownBy(() -> harness.castFromExile(player1, divination.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player1, island.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(divination, island);
    }

    @Test
    @DisplayName("An empty library does not prevent the ten mana from being added")
    void emptyLibraryStillAddsMana() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ApexOfPower()));
        addApexMana(player1);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(10);
        harness.assertInGraveyard(player1, "Apex of Power");
    }

    @Test
    @DisplayName("Exile permission preserves sorcery timing and expires after the turn")
    void permissionPreservesTimingAndExpires() {
        Divination divination = new Divination();
        harness.setLibrary(player1, List.of(divination));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new ApexOfPower()));
        addApexMana(player1);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleListChoice(player1, "BLUE");
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLUE, 3);
        assertThatThrownBy(() -> harness.castFromExile(player1, divination.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(divination.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(divination);
    }

}
