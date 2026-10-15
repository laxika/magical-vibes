package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChopDown;
import com.github.laxika.magicalvibes.cards.g.GiantKiller;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysteriousPathlighter.class, GiantKiller.class, ChopDown.class, YouthfulKnight.class})
class MysteriousPathlighterTest extends BaseCardTest {

    @Test
    void creatureWithAdventureEntersWithAnAdditionalCounterEvenWhenCastAsCreature() {
        harness.addToBattlefield(player1, new MysteriousPathlighter());

        harness.castFromHand(player1, new GiantKiller(), "{W}");
        harness.passBothPriorities();

        Permanent giantKiller = findPermanent(player1, "Giant Killer");

        assertThat(giantKiller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonAdventureCreaturesAndOpponentsCreaturesDoNotGetAnAdditionalCounter() {
        harness.addToBattlefield(player1, new MysteriousPathlighter());

        harness.castFromHand(player1, new YouthfulKnight(), "{1}{W}");
        harness.passBothPriorities();
        Permanent ownKnight = findPermanent(player1, "Youthful Knight");

        gd.activePlayerId = player2.getId();
        harness.castFromHand(player2, new GiantKiller(), "{W}");
        harness.passBothPriorities();
        Permanent opposingGiantKiller = findPermanent(player2, "Giant Killer");

        assertThat(ownKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingGiantKiller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multiplePathlightersEachAddACounter() {
        harness.addToBattlefield(player1, new MysteriousPathlighter());
        harness.addToBattlefield(player1, new MysteriousPathlighter());

        harness.castFromHand(player1, new GiantKiller(), "{W}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Giant Killer").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void pathlighterDoesNotAddCountersToCreaturesAlreadyOnTheBattlefieldOrToItself() {
        Permanent giantKiller = harness.addToBattlefieldAndReturn(player1, new GiantKiller());

        harness.castFromHand(player1, new MysteriousPathlighter(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(giantKiller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Mysterious Pathlighter").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void creatureCastAfterItsAdventureEntersWithACounter() {
        harness.addToBattlefield(player1, new MysteriousPathlighter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        GiantKiller giantKiller = new GiantKiller();
        harness.setHand(player1, List.of(giantKiller));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(giantKiller.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, giantKiller.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Giant Killer").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }
}
