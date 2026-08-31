package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoultetherGolem.class, GrizzlyBears.class})
class SoultetherGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one time counter")
    void entersWithOneTimeCounter() {
        harness.setHand(player1, List.of(new SoultetherGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Soultether Golem");

        assertThat(golem.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a time counter on itself when another creature enters under its controller's control")
    void gainsTimeCounterWhenAnotherCreatureEnters() {
        Permanent golem = addCreatureReady(player1, new SoultetherGolem());
        golem.setCounterCount(CounterType.TIME, 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(golem.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for itself entering the battlefield")
    void doesNotTriggerForItselfEntering() {
        harness.setHand(player1, List.of(new SoultetherGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Soultether Golem");
        assertThat(golem.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes one time counter during upkeep and sacrifices when the last is removed")
    void vanishesDuringUpkeep() {
        Permanent golem = addCreatureReady(player1, new SoultetherGolem());
        golem.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(golem.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Soultether Golem");
        harness.assertInGraveyard(player1, "Soultether Golem");
    }
}
