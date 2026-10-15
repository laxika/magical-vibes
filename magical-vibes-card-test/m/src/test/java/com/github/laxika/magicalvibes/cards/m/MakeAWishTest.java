package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RakshasaVizier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MakeAWish.class, DarkthicketWolf.class, AvacynsPilgrim.class, Forest.class, Mulch.class})
class MakeAWishTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two cards at random from graveyard to hand")
    void returnsTwoCardsFromGraveyard() {
        harness.setGraveyard(player1, List.of(new DarkthicketWolf(), new AvacynsPilgrim(), new MakeAWish()));
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Two of the three graveyard cards should be in hand now
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // 1 remaining card + Make a Wish itself goes to graveyard after resolution
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Returns only one card when graveyard has only one card")
    void returnsOneCardWhenGraveyardHasOnlyOne() {
        harness.setGraveyard(player1, List.of(new DarkthicketWolf()));
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInHand(player1, "Darkthicket Wolf");
        // Only Make a Wish itself in graveyard after resolution
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Make a Wish");
    }

    @Test
    @DisplayName("Returns exactly two when graveyard has exactly two cards")
    void returnsTwoWhenGraveyardHasExactlyTwo() {
        harness.setGraveyard(player1, List.of(new DarkthicketWolf(), new AvacynsPilgrim()));
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Darkthicket Wolf");
        harness.assertInHand(player1, "Avacyn's Pilgrim");
        // Only Make a Wish itself in graveyard after resolution
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Make a Wish");
    }

    @Test
    @DisplayName("Does nothing when graveyard is empty")
    void doesNothingWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Make a Wish goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setGraveyard(player1, List.of(new DarkthicketWolf(), new AvacynsPilgrim()));
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Make a Wish");
    }
    @Test
    @DisplayName("Returns lands and noncreature spells without touching the opponent's graveyard")
    void returnsNoncreatureCardsOnlyFromControllersGraveyard() {
        Forest land = new Forest();
        Mulch sorcery = new Mulch();
        DarkthicketWolf opposingCard = new DarkthicketWolf();
        MakeAWish wish = new MakeAWish();
        harness.setGraveyard(player1, List.of(land, sorcery));
        harness.setGraveyard(player2, List.of(opposingCard));
        harness.setHand(player1, List.of(wish));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(land, sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(wish);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @CardUsed({RakshasaVizier.class})
    @DisplayName("Returning cards to hand does not trigger graveyard exile abilities")
    void returningCardsDoesNotTriggerExileAbilities() {
        var vizier = harness.addToBattlefieldAndReturn(player1, new RakshasaVizier());
        Forest land = new Forest();
        Mulch sorcery = new Mulch();
        harness.setGraveyard(player1, List.of(land, sorcery));
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(land, sorcery);
        assertThat(vizier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}