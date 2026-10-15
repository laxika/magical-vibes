package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DidntSayPlease;
import com.github.laxika.magicalvibes.cards.v.VentureDeeper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkSecretkeeper.class, VentureDeeper.class, DidntSayPlease.class})
class MerfolkSecretkeeperTest extends BaseCardTest {

    @Test
    void adventureMillsFourCardsAndExilesTheCard() {
        List<Card> milled = List.of(new MerfolkSecretkeeper(), new MerfolkSecretkeeper(), new MerfolkSecretkeeper(), new MerfolkSecretkeeper());
        harness.setLibrary(player2, milled);
        MerfolkSecretkeeper card = new MerfolkSecretkeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrderElementsOf(milled);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        MerfolkSecretkeeper card = new MerfolkSecretkeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Merfolk Secretkeeper");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCanTargetItsControllerAndLeavesCardsBelowTheTopFour() {
        List<Card> milled = List.of(new MerfolkSecretkeeper(), new MerfolkSecretkeeper(),
                new MerfolkSecretkeeper(), new MerfolkSecretkeeper());
        Card remaining = new MerfolkSecretkeeper();
        harness.setLibrary(player1, List.of(milled.get(0), milled.get(1), milled.get(2), milled.get(3), remaining));
        MerfolkSecretkeeper card = new MerfolkSecretkeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(milled);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureMillsAllAvailableCardsFromAShortLibrary() {
        List<Card> milled = List.of(new MerfolkSecretkeeper(), new MerfolkSecretkeeper());
        harness.setLibrary(player2, milled);
        MerfolkSecretkeeper card = new MerfolkSecretkeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrderElementsOf(milled);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureCanBeCastDirectlyWithoutMilling() {
        Card libraryCard = new MerfolkSecretkeeper();
        harness.setLibrary(player2, List.of(libraryCard));
        MerfolkSecretkeeper card = new MerfolkSecretkeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Merfolk Secretkeeper");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void counteredAdventureGoesToGraveyardWithoutMillingItsTargetOrGrantingExilePermission() {
        Card libraryCard = new MerfolkSecretkeeper();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setLibrary(player1, List.of());
        MerfolkSecretkeeper card = new MerfolkSecretkeeper();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(new DidntSayPlease()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, card.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void adventureResolvesAgainstAnEmptyLibraryAndStillGrantsCreatureCastingPermission() {
        harness.setLibrary(player2, List.of());
        MerfolkSecretkeeper card = new MerfolkSecretkeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }
}
