package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Bloodghast;
import com.github.laxika.magicalvibes.cards.c.CarnageAltar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HedronCrab;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.s.SpellPierce;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousTrap.class, KrakenHatchling.class, SpellPierce.class, HedronCrab.class,
        Forest.class, Bloodghast.class, CarnageAltar.class})
class RavenousTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target player's graveyard")
    void exilesTargetPlayersGraveyard() {
        Card ownCard = new KrakenHatchling();
        Card targetCard = new SpellPierce();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(targetCard));
        harness.setHand(player1, List.of(new RavenousTrap()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(targetCard.getId());
        harness.assertInGraveyard(player1, "Ravenous Trap");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(ownCard.getId());
    }

    @Test
    @DisplayName("Can be cast for no mana after an opponent put three cards into their graveyard this turn")
    void castsForFreeAfterOpponentPutThreeCardsIntoGraveyard() {
        Card first = new KrakenHatchling();
        Card second = new SpellPierce();
        Card third = new KrakenHatchling();
        harness.setGraveyard(player2, List.of(first, second, third));
        gd.cardsPutIntoGraveyardFromAnywhereThisTurn.put(
                player2.getId(), Set.of(first.getId(), second.getId(), third.getId()));
        harness.setHand(player1, List.of(new RavenousTrap()));

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the alternate cost for cards that were already in an opponent's graveyard")
    void alternateCostRequiresCardsPutIntoGraveyardThisTurn() {
        harness.setGraveyard(player2, List.of(new KrakenHatchling(), new SpellPierce(), new KrakenHatchling()));
        harness.setHand(player1, List.of(new RavenousTrap()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void alternateCostRequiresAtLeastThreeCards() {
        Card first = new KrakenHatchling();
        Card second = new SpellPierce();
        harness.setGraveyard(player2, List.of(first, second));
        gd.cardsPutIntoGraveyardFromAnywhereThisTurn.put(player2.getId(), Set.of(first.getId(), second.getId()));
        harness.setHand(player1, List.of(new RavenousTrap()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ownGraveyardEntriesDoNotEnableAlternateCost() {
        Card first = new KrakenHatchling();
        Card second = new SpellPierce();
        Card third = new KrakenHatchling();
        harness.setGraveyard(player1, List.of(first, second, third));
        gd.cardsPutIntoGraveyardFromAnywhereThisTurn.put(
                player1.getId(), Set.of(first.getId(), second.getId(), third.getId()));
        harness.setHand(player1, List.of(new RavenousTrap()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canExileOwnGraveyard() {
        Card card = new KrakenHatchling();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new RavenousTrap()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId).containsExactly(card.getId());
        harness.assertInGraveyard(player1, "Ravenous Trap");
    }

    @Test
    void canTargetEmptyGraveyard() {
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new RavenousTrap()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ravenous Trap");
    }

    @Test
    void actualMillingEnablesAlternateCostEvenAfterGraveyardIsExiled() {
        harness.addToBattlefield(player1, new HedronCrab());
        List<Card> milled = List.of(new KrakenHatchling(), new SpellPierce(), new Forest());
        harness.setLibrary(player2, milled);
        harness.setHand(player1, List.of(new Forest(), new RavenousTrap(), new RavenousTrap()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(milled.stream().map(Card::getId).toList());
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void repeatedEntriesOfSameCardEnableAlternateCost() {
        harness.addToBattlefield(player2, new CarnageAltar());
        Bloodghast bloodghast = new Bloodghast();
        harness.addToBattlefield(player2, bloodghast);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 9);
        harness.setHand(player1, List.of(new RavenousTrap()));

        for (int entry = 0; entry < 3; entry++) {
            harness.activateAbility(player2, 0, null, null);
            harness.passBothPriorities();
            if (entry < 2) {
                harness.enterBattlefieldAndReturn(player2, new Forest());
                harness.passBothPriorities();
                harness.handleMayAbilityChosen(player2, true);
            }
        }

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .containsExactly(bloodghast.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void exilesCardsThatEnterGraveyardWhileTrapIsOnStack() {
        harness.addToBattlefield(player1, new HedronCrab());
        List<Card> milled = List.of(new KrakenHatchling(), new SpellPierce(), new Forest());
        harness.setLibrary(player2, milled);
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new RavenousTrap()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, player2.getId());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(milled.stream().map(Card::getId).toList());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ravenous Trap");
    }
}
