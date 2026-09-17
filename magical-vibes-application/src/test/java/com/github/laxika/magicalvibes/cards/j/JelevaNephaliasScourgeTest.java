package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JelevaNephaliasScourge.class, Divination.class, GrizzlyBears.class})
class JelevaNephaliasScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield exiles as many cards from each library as mana spent")
    void entersAndExilesCardsBasedOnManaSpent() {
        List<Card> ownLibrary = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        List<Card> opponentLibrary = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, ownLibrary);
        harness.setLibrary(player2, opponentLibrary);
        harness.setHand(player1, List.of(new JelevaNephaliasScourge()));
        addJelevaMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jeleva = findPermanent(player1, "Jeleva, Nephalia's Scourge");
        assertThat(gd.getCardsExiledByPermanent(jeleva.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking offers one exiled instant or sorcery for free")
    void attackingOffersOneExiledInstantOrSorceryForFree() {
        Divination divination = new Divination();
        Card ownCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(divination, ownCreature));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new JelevaNephaliasScourge()));
        addJelevaMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jeleva = findPermanent(player1, "Jeleva, Nephalia's Scourge");
        jeleva.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities)
                .extracting(PendingMayAbility::targetCardId)
                .containsExactly(divination.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.getCardsExiledByPermanent(jeleva.getId())).contains(ownCreature);
    }

    private void addJelevaMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
