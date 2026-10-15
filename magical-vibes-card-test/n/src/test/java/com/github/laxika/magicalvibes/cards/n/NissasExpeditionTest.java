package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RadiantFountain;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissasExpedition.class, Forest.class, Plains.class, ElvishMystic.class, RadiantFountain.class})
class NissasExpeditionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only basic lands, destination battlefield tapped")
    void offersOnlyBasicLands() {
        castExpedition();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC))
                .noneMatch(c -> c.getName().equals("Elvish Mystic"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Both chosen basic lands enter the battlefield tapped")
    void bothChosenLandsEnterTapped() {
        castExpedition();

        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .allMatch(p -> p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"));
        harness.assertInGraveyard(player1, "Nissa's Expedition");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        castExpedition();

        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castExpedition() {
        harness.castFromHand(player1, new NissasExpedition(), "{G}{G}{G}{G}{G}");
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new ElvishMystic()));
    }

    @Test
    void mayFindTwoLandsWithTheSameNameButNeverMoreThanTwo() {
        castExpedition();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.passBothPriorities();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Forest"))
                .hasSize(2).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Nissa's Expedition");
    }

    @Test
    void chosenLandsWaitUntilSearchIsComplete() {
        castExpedition();
        harness.passBothPriorities();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayChooseOnlyOneLandEvenWhenTwoAreAvailable() {
        castExpedition();
        harness.passBothPriorities();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Plains"))
                .singleElement().satisfies(p -> assertThat(p.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(c -> c.getName()).containsExactlyInAnyOrder("Forest", "Elvish Mystic");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Nissa's Expedition");
    }

    @Test
    void resolvesWhenOnlyOneBasicLandIsAvailable() {
        castExpedition();
        harness.setLibrary(player1, List.of(new Forest(), new ElvishMystic()));
        harness.passBothPriorities();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Forest"))
                .singleElement().satisfies(p -> assertThat(p.isTapped()).isTrue());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Nissa's Expedition");
    }

    @Test
    void cannotFindNonbasicLands() {
        castExpedition();
        harness.setLibrary(player1, List.of(new RadiantFountain(), new Forest()));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(c -> c.getName()).containsExactly("Forest");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Radiant Fountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithEmptyLibrary() {
        castExpedition();
        harness.setLibrary(player1, List.of());
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Nissa's Expedition");
    }

    @Test
    void resolvesWithNoBasicLands() {
        castExpedition();
        harness.setLibrary(player1, List.of(new RadiantFountain(), new ElvishMystic()));
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Nissa's Expedition");
    }

    @Test
    void summoningSickGreenCreatureCanConvokeColoredManaWithoutProducingMana() {
        harness.setHand(player1, List.of(new NissasExpedition()));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        mystic.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(mystic.getId()));

        assertThat(mystic.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Nissa's Expedition");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void greenCreaturesCanConvokeTheEntireCost() {
        harness.setHand(player1, List.of(new NissasExpedition()));
        harness.setLibrary(player1, List.of());
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new ElvishMystic()))
                .toList();

        harness.castInstantWithConvoke(player1, 0, List.of(), creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Nissa's Expedition");
    }
}
