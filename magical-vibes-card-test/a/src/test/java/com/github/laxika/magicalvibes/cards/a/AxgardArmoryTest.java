package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.s.StalwartValkyrie;
import com.github.laxika.magicalvibes.cards.s.SpectralSteel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AxgardArmory.class, SpectralSteel.class, GoldveinPick.class, StalwartValkyrie.class})
class AxgardArmoryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for white mana")
    void entersTappedAndTapsForWhite() {
        harness.setHand(player1, List.of(new AxgardArmory()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Axgard Armory").isTapped()).isTrue();

        findPermanent(player1, "Axgard Armory").untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Searches for one Aura and one Equipment, then shuffles")
    void searchesForAuraAndEquipment() {
        Card aura = new SpectralSteel();
        Card equipment = new GoldveinPick();
        harness.addToBattlefield(player1, new AxgardArmory());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(aura, equipment, new StalwartValkyrie()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch auraSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(auraSearch.params().cards()).extracting(Card::getId).containsExactly(aura.getId());
        assertThat(auraSearch.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch equipmentSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(equipmentSearch.params().cards()).extracting(Card::getId).containsExactly(equipment.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(aura.getId(), equipment.getId());
        harness.assertInGraveyard(player1, "Axgard Armory");
    }

    @Test
    @DisplayName("Finds an Equipment when no Aura is in the library")
    void findsEquipmentWithoutAura() {
        Card equipment = new GoldveinPick();
        harness.addToBattlefield(player1, new AxgardArmory());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StalwartValkyrie(), equipment));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getId).containsExactly(equipment.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(equipment.getId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("May find only an Aura or only an Equipment even when both are available")
    void mayDeclineEitherSearch(boolean chooseAura) {
        Card aura = new SpectralSteel();
        Card equipment = new GoldveinPick();
        prepareSearch(List.of(aura, equipment));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, chooseAura ? 0 : -1);
        harness.handleCardChosen(player1, chooseAura ? -1 : 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly((chooseAura ? aura : equipment).getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly((chooseAura ? equipment : aura).getId());
        assertThat(gameLogContains("shuffles their library")).isTrue();
    }

    @Test
    @DisplayName("May decline both searches and still shuffles")
    void mayFindNeitherCard() {
        Card aura = new SpectralSteel();
        Card equipment = new GoldveinPick();
        prepareSearch(List.of(aura, equipment));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(aura.getId(), equipment.getId());
        assertThat(gameLogContains("shuffles their library")).isTrue();
    }

    @Test
    @DisplayName("Pays mana and sacrifices the land before the search resolves")
    void paysCostsBeforeResolution() {
        prepareSearch(List.of(new SpectralSteel()));

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Axgard Armory");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Spectral Steel");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("shuffles their library")).isTrue();
    }

    @Test
    @DisplayName("Resolves and shuffles when neither card type is present")
    void noMatchingCards() {
        Card creature = new StalwartValkyrie();
        prepareSearch(List.of(creature));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).containsExactly(creature.getId());
        assertThat(gameLogContains("shuffles their library")).isTrue();
    }

    private void prepareSearch(List<Card> library) {
        harness.addToBattlefield(player1, new AxgardArmory());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Cannot activate the search while the land is tapped")
    void cannotSearchWhileTapped() {
        prepareSearch(List.of(new SpectralSteel()));
        findPermanent(player1, "Axgard Armory").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Axgard Armory");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent activation or completion")
    void searchesEmptyLibrary() {
        prepareSearch(List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Axgard Armory");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("shuffles their library")).isTrue();
    }
}
