package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlpackWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TorgalAFineHound.class, EliteVanguard.class, GrizzlyBears.class, HowlpackWolf.class})
class TorgalAFineHoundTest extends BaseCardTest {

    @Test
    @DisplayName("The first Human creature spell enters with a counter for each Dog and Wolf")
    void firstHumanCreatureEntersWithDogAndWolfCounters() {
        addReadyTorgal();
        harness.addToBattlefield(player1, new HowlpackWolf());

        EliteVanguard human = new EliteVanguard();
        harness.setHand(player1, List.of(human));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(human).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the first Human creature spell each turn gets counters")
    void onlyFirstHumanCreatureSpellTriggers() {
        addReadyTorgal();

        EliteVanguard firstHuman = new EliteVanguard();
        EliteVanguard secondHuman = new EliteVanguard();
        harness.setHand(player1, List.of(firstHuman, secondHuman));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(firstHuman).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(secondHuman).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Non-Human creature spells do not trigger Torgal")
    void nonHumanCreatureSpellDoesNotTrigger() {
        addReadyTorgal();

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(bears).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Torgal taps for one mana of the chosen color")
    void tapsForAnyColor() {
        Permanent torgal = addReadyTorgal();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(torgal.isTapped()).isTrue();
    }

    private Permanent addReadyTorgal() {
        Permanent torgal = harness.addToBattlefieldAndReturn(player1, new TorgalAFineHound());
        torgal.setSummoningSick(false);
        return torgal;
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
