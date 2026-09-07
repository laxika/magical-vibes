package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PolukranosUnchained.class, GrizzlyBears.class, Shock.class})
class PolukranosUnchainedTest extends BaseCardTest {

    @Test
    void entersWithSixCountersFromHand() {
        harness.setHand(player1, List.of(new PolukranosUnchained()));
        addPolukranosMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent polukranos = findPermanent(player1, "Polukranos, Unchained");
        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void escapesWithTwelveCountersAndExilesSixOtherCards() {
        PolukranosUnchained polukranos = new PolukranosUnchained();
        List<Card> otherCards = IntStream.range(0, 6)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
        harness.setGraveyard(player1, List.of(polukranos, otherCards.get(0), otherCards.get(1),
                otherCards.get(2), otherCards.get(3), otherCards.get(4), otherCards.get(5)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyard(player1, 0, IntStream.rangeClosed(1, 6).boxed().toList());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);

        harness.passBothPriorities();

        Permanent escaped = findPermanent(player1, "Polukranos, Unchained");
        assertThat(escaped.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(12);
        assertThat(escaped.isEscaped()).isTrue();
    }

    @Test
    void damageIsPreventedAndRemovesCountersWhileCountered() {
        Permanent polukranos = harness.addToBattlefieldAndReturn(player2, new PolukranosUnchained());
        polukranos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, polukranos.getId());
        harness.passBothPriorities();

        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(polukranos.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Polukranos, Unchained");
    }

    @Test
    void damageIsDealtNormallyWithoutCounters() {
        PolukranosUnchained card = new PolukranosUnchained();
        card.setToughness(3);
        Permanent polukranos = harness.addToBattlefieldAndReturn(player2, card);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, polukranos.getId());
        harness.passBothPriorities();

        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(polukranos.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Polukranos, Unchained");
    }

    @Test
    void abilityMakesPolukranosFightAnotherCreature() {
        Permanent polukranos = harness.addToBattlefieldAndReturn(player1, new PolukranosUnchained());
        polukranos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(polukranos.getMarkedDamage()).isZero();
    }

    private void addPolukranosMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
