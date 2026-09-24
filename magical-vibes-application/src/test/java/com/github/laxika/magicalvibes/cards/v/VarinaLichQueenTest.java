package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VarinaLichQueen.class, Gravecrawler.class, GrizzlyBears.class, Forest.class, Shock.class})
class VarinaLichQueenTest extends BaseCardTest {

    @Test
    void attacksWithZombiesDrawDiscardAndGainLifeByZombieCount() {
        addCreatureReady(player1, new VarinaLichQueen());
        addCreatureReady(player1, new Gravecrawler());
        addCreatureReady(player1, new GrizzlyBears());
        Card firstDraw = new Forest();
        Card secondDraw = new Shock();
        Card remainingCard = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(firstDraw, secondDraw, remainingCard));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void doesNotTriggerWithoutAnAttackingZombie() {
        addCreatureReady(player1, new VarinaLichQueen());
        addCreatureReady(player1, new GrizzlyBears());
        Card card = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(card);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
    }

    @Test
    void exilesTwoGraveyardCardsAndCreatesTappedZombie() {
        harness.addToBattlefield(player1, new VarinaLichQueen());
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Shock();
        harness.setGraveyard(player1, List.of(firstCard, secondCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstCard, secondCard);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    void cannotActivateWithoutTwoCardsInGraveyard() {
        harness.addToBattlefield(player1, new VarinaLichQueen());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough");
    }
}
