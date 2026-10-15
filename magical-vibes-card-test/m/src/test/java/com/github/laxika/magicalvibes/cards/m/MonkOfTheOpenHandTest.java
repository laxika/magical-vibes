package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonkOfTheOpenHand.class, LightningBolt.class})
class MonkOfTheOpenHandTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell each turn puts a +1/+1 counter on Monk of the Open Hand")
    void secondSpellPutsCounterOnMonk() {
        Permanent monk = addCreatureReady(player1, new MonkOfTheOpenHand());

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Monk cast first counts itself, and the Monk cast second does not trigger itself")
    void monkCastFirstCountsAsFirstSpell() {
        MonkOfTheOpenHand firstCard = new MonkOfTheOpenHand();
        MonkOfTheOpenHand secondCard = new MonkOfTheOpenHand();
        MonkOfTheOpenHand thirdCard = new MonkOfTheOpenHand();
        harness.setHand(player1, List.of(firstCard, secondCard, thirdCard));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent first = findPermanent(player1, "Monk of the Open Hand");
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Monk of the Open Hand")).isEqualTo(1);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Monk of the Open Hand")).isEqualTo(3);
        assertThat(findPermanents(player1, "Monk of the Open Hand"))
                .filteredOn(p -> p.getCard().getId().equals(secondCard.getId())
                        || p.getCard().getId().equals(thirdCard.getId()))
                .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Monk entering after the second spell does not trigger on the third spell")
    void enteringAsSecondSpellDoesNotTriggerLater() {
        harness.setHand(player1, List.of(new LightningBolt(), new MonkOfTheOpenHand(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent monk = findPermanent(player1, "Monk of the Open Hand");
        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponents' spells do not count, and the controller can trigger again on the opponent's turn")
    void spellCountResetsAndIgnoresOpponentSpells() {
        Permanent monk = addCreatureReady(player1, new MonkOfTheOpenHand());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(),
                new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each Monk's trigger affects its own source and does nothing if that source has died")
    void removingOneMonkDoesNotRedirectItsCounter() {
        Permanent first = addCreatureReady(player1, new MonkOfTheOpenHand());
        Permanent second = addCreatureReady(player1, new MonkOfTheOpenHand());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first).contains(second);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
