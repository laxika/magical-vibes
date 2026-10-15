package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NorthernAirTemple;
import com.github.laxika.magicalvibes.cards.t.TurtleDuck;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SouthernAirTemple.class, GrizzlyBears.class, NorthernAirTemple.class, TurtleDuck.class})
class SouthernAirTempleTest extends BaseCardTest {

    @Test
    @DisplayName("Its entry puts one counter per Shrine on each creature you control")
    void entryCountsControlledShrines() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, shrine());
        harness.addToBattlefield(player1, shrine());
        harness.castFromHand(player1, new SouthernAirTemple(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Puts a counter on each creature when another Shrine enters")
    void anotherShrineEnteringPutsCounters() {
        harness.addToBattlefield(player1, new SouthernAirTemple());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, shrine());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when a non-Shrine creature enters")
    void nonShrineEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new SouthernAirTemple());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entryCountsItselfButNotOpposingShrinesOrNoncreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent otherShrine = harness.addToBattlefieldAndReturn(player1, new NorthernAirTemple());
        harness.addToBattlefield(player2, new NorthernAirTemple());

        Permanent temple = harness.enterBattlefieldAndReturn(player1, new SouthernAirTemple());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherShrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(temple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void anotherShrineGivesExactlyOneCounterToEveryControlledCreature() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new SouthernAirTemple());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());

        Permanent shrine = harness.enterBattlefieldAndReturn(player1, new NorthernAirTemple());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(temple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(shrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opposingShrineEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new SouthernAirTemple());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());

        harness.enterBattlefieldAndReturn(player2, new NorthernAirTemple());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entryCountsShrinesAndCreaturesAtResolution() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new NorthernAirTemple());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        harness.enterBattlefieldAndReturn(player1, new SouthernAirTemple());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(shrine);
        gd.playerGraveyards.get(player1.getId()).add(shrine.getCard());
        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new TurtleDuck());

        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entryTriggerStillResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new NorthernAirTemple());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent temple = harness.enterBattlefieldAndReturn(player1, new SouthernAirTemple());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(temple);
        gd.playerGraveyards.get(player1.getId()).add(temple.getCard());

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Card shrine() {
        Card card = new Card();
        card.setName("Test Shrine");
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SHRINE));
        return card;
    }
}
