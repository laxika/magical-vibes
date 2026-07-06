package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InsectileAberration;
import com.github.laxika.magicalvibes.cards.r.RottingFensnake;
import com.github.laxika.magicalvibes.cards.s.ScreechingBat;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlechLoafingPestTest extends BaseCardTest {

    

    @Test
    @DisplayName("Puts +1/+1 counters on each matching creature you control when you gain life")
    void putsCountersOnMatchingCreatures() {
        harness.addToBattlefield(player1, new BlechLoafingPest());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player1, new ScreechingBat());
        harness.addToBattlefield(player1, new RottingFensnake());
        harness.addToBattlefield(player1, new InsectileAberration());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Angel of Mercy (ETB gain 3 life)
        harness.passBothPriorities(); // resolve GainLifeEffect
        harness.passBothPriorities(); // resolve Blech's triggered ability

        assertThat(findPermanent(player1, "Blech, Loafing Pest")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Giant Spider")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Screeching Bat")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Rotting Fensnake")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Insectile Aberration")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not put counters on opponent's matching creatures")
    void doesNotPutCountersOnOpponentCreatures() {
        harness.addToBattlefield(player1, new BlechLoafingPest());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve Angel of Mercy
        harness.passBothPriorities(); // resolve GainLifeEffect

        assertThat(findPermanent(player2, "Giant Spider")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Blech, Loafing Pest")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when only the opponent gains life")
    void noCountersWhenOpponentGainsLife() {
        harness.addToBattlefield(player1, new BlechLoafingPest());
        harness.addToBattlefield(player1, new GiantSpider());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve Angel of Mercy
        harness.passBothPriorities(); // resolve GainLifeEffect

        Permanent blech = findPermanent(player1, "Blech, Loafing Pest");
        Permanent spider = findPermanent(player1, "Giant Spider");
        assertThat(blech.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
