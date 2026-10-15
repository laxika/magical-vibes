package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScaleguardSentinels.class, StormwingDragon.class})
class ScaleguardSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when a Dragon is revealed from hand")
    void entersWithCounterWhenDragonIsRevealed() {
        ScaleguardSentinels sentinels = new ScaleguardSentinels();
        StormwingDragon dragon = new StormwingDragon();
        harness.setHand(player1, List.of(sentinels, dragon));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(findSentinels().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
    }

    @Test
    @DisplayName("Enters with a +1/+1 counter when a Dragon was controlled as cast")
    void entersWithCounterWhenDragonWasControlledAsCast() {
        addCreatureReady(player1, new StormwingDragon());
        harness.setHand(player1, List.of(new ScaleguardSentinels()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findSentinels().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters without a counter when no Dragon was revealed or controlled")
    void entersWithoutCounterWithoutDragon() {
        harness.setHand(player1, List.of(new ScaleguardSentinels()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findSentinels().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A Dragon leaving before resolution does not prevent the counter")
    void retainsCastTimeDragonCondition() {
        Permanent dragon = addCreatureReady(player1, new StormwingDragon());
        harness.setHand(player1, List.of(new ScaleguardSentinels()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(dragon);
        gd.playerGraveyards.get(player1.getId()).add(dragon.getCard());
        harness.passBothPriorities();

        assertThat(findSentinels().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Dragon arriving after casting does not grant a counter")
    void dragonMustBeControlledAtCastTime() {
        harness.setHand(player1, List.of(new ScaleguardSentinels()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        addCreatureReady(player1, new StormwingDragon());
        harness.passBothPriorities();

        assertThat(findSentinels().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Revealing and controlling a Dragon still grants only one counter")
    void bothConditionsGrantOnlyOneCounter() {
        addCreatureReady(player1, new StormwingDragon());
        StormwingDragon dragon = new StormwingDragon();
        harness.setHand(player1, List.of(new ScaleguardSentinels(), dragon));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(findSentinels().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dragon);
    }

    @Test
    @DisplayName("Having a Dragon in hand does not require revealing it")
    void mayDeclineToRevealDragon() {
        StormwingDragon dragon = new StormwingDragon();
        harness.setHand(player1, List.of(new ScaleguardSentinels(), dragon));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findSentinels().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dragon);
    }

    @Test
    @DisplayName("An opponent's Dragon does not satisfy the condition")
    void opponentsDragonDoesNotGrantCounter() {
        addCreatureReady(player2, new StormwingDragon());
        harness.setHand(player1, List.of(new ScaleguardSentinels()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findSentinels().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent findSentinels() {
        return findPermanent(player1, "Scaleguard Sentinels");
    }
}
