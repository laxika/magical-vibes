package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FinneasAceArcher.class)
class FinneasAceArcherTest extends BaseCardTest {

    @Test
    void attacksPutCountersOnOtherControlledTokensAndRabbitsOnly() {
        Permanent finneas = addCreatureReady(player1, new FinneasAceArcher());
        Permanent token = addToken(player1, "Rabbit token", List.of());
        Permanent rabbit = addCreature(player1, "Rabbit", List.of(CardSubtype.RABBIT), false, 1, 1);
        Permanent ordinaryCreature = addCreature(player1, "Bear", List.of(CardSubtype.BEAR), false, 1, 1);
        Permanent opposingToken = addToken(player2, "Opposing token", List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(finneas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(rabbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ordinaryCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingToken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void drawsAfterCountersRaiseTotalPowerToTen() {
        Permanent finneas = addCreatureReady(player1, new FinneasAceArcher());
        Permanent token = addToken(player1, "Large token", List.of(), 7, 7);
        Card drawnCard = new Card();
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).addFirst(drawnCard);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(finneas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotDrawWhenTotalPowerRemainsBelowTen() {
        addCreatureReady(player1, new FinneasAceArcher());
        addToken(player1, "Small token", List.of());
        Card deckCard = new Card();
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).addFirst(deckCard);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(deckCard);
    }

    private Permanent addCreature(Player player, String name, List<CardSubtype> subtypes,
                                  boolean token, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(subtypes);
        card.setToken(token);
        card.setPower(power);
        card.setToughness(toughness);

        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addToken(Player player, String name, List<CardSubtype> subtypes) {
        return addToken(player, name, subtypes, 1, 1);
    }

    private Permanent addToken(Player player, String name, List<CardSubtype> subtypes,
                               int power, int toughness) {
        return addCreature(player, name, subtypes, true, power, toughness);
    }
}
