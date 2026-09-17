package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarlovOfTheGhostCouncil.class, AngelOfMercy.class, GrizzlyBears.class, Forest.class})
class KarlovOfTheGhostCouncilTest extends BaseCardTest {

    @Test
    @DisplayName("Gains two +1/+1 counters when its controller gains life")
    void gainsTwoCountersOnLifeGain() {
        harness.addToBattlefield(player1, new KarlovOfTheGhostCouncil());
        Permanent karlov = findPermanent(player1, "Karlov of the Ghost Council");

        harness.setHand(player1, java.util.List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(karlov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes six +1/+1 counters and exiles target creature")
    void removesSixCountersAndExilesTargetCreature() {
        Permanent karlov = addCreatureReady(player1, new KarlovOfTheGhostCouncil());
        karlov.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(karlov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot activate without six +1/+1 counters")
    void requiresSixCounters() {
        Permanent karlov = addCreatureReady(player1, new KarlovOfTheGhostCouncil());
        karlov.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Permanent karlov = addCreatureReady(player1, new KarlovOfTheGhostCouncil());
        karlov.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
