package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TatteredRatter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CruelSomnophage.class, CantWakeUp.class, TatteredRatter.class, Forest.class})
class CruelSomnophageTest extends BaseCardTest {

    @Test
    void adventureMillsFourCardsAndExilesTheCard() {
        List<Card> milled = List.of(new TatteredRatter(), new TatteredRatter(), new TatteredRatter(), new TatteredRatter());
        harness.setLibrary(player2, milled);
        CruelSomnophage card = new CruelSomnophage();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrderElementsOf(milled);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCountsCreatureCardsInAllGraveyards() {
        Permanent somnophage = addCreatureReady(player1, new CruelSomnophage());
        harness.setGraveyard(player1, List.of(new TatteredRatter(), new TatteredRatter(), new Forest()));
        harness.setGraveyard(player2, List.of(new TatteredRatter(), new TatteredRatter(), new TatteredRatter(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, somnophage)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, somnophage)).isEqualTo(5);
    }

    @Test
    void creatureFaceUpdatesAsCreatureCardsEnterAndLeaveGraveyards() {
        Permanent somnophage = addCreatureReady(player1, new CruelSomnophage());
        harness.setGraveyard(player1, List.of(new TatteredRatter()));
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());

        assertThat(gqs.getEffectivePower(gd, somnophage)).isEqualTo(1);

        graveyard.add(new TatteredRatter());
        assertThat(gqs.getEffectivePower(gd, somnophage)).isEqualTo(2);

        graveyard.removeFirst();
        assertThat(gqs.getEffectivePower(gd, somnophage)).isEqualTo(1);
    }

    @Test
    void adventureCanTargetItsControllerAndLeavesTheFifthCardInTheLibrary() {
        List<Card> milled = List.of(new TatteredRatter(), new Forest(), new CruelSomnophage(), new Forest());
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(milled.get(0), milled.get(1), milled.get(2), milled.get(3), remaining));
        CruelSomnophage card = new CruelSomnophage();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureMillsAllAvailableCardsWhenTheLibraryHasFewerThanFour() {
        List<Card> milled = List.of(new TatteredRatter(), new Forest());
        harness.setLibrary(player2, milled);
        CruelSomnophage card = new CruelSomnophage();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void creatureCanBeCastFromExileAfterTheAdventureResolves() {
        harness.setLibrary(player2, List.of(new TatteredRatter(), new Forest(), new Forest(), new Forest()));
        CruelSomnophage card = new CruelSomnophage();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        Permanent somnophage = findPermanent(player1, "Cruel Somnophage");
        assertThat(somnophage.getCard().getId()).isEqualTo(card.getId());
        assertThat(gqs.getEffectivePower(gd, somnophage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, somnophage)).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void creatureDiesWhenNoCreatureCardsAreInAnyGraveyard() {
        CruelSomnophage card = new CruelSomnophage();
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(1);
    }

    @Test
    void characteristicAbilityWorksInHandAndGraveyardAndCountsAdventureCreatures() {
        CruelSomnophage card = new CruelSomnophage();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(new CruelSomnophage(), new Forest()));
        harness.setGraveyard(player2, List.of(new TatteredRatter()));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(2);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(card, new CruelSomnophage(), new Forest()));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(3);
    }
}
