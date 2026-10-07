package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.Eject;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlpackWolf;
import com.github.laxika.magicalvibes.cards.i.IronGiant;
import com.github.laxika.magicalvibes.cards.i.ItemShopkeep;
import com.github.laxika.magicalvibes.cards.r.RinoaHeartilly;
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

@CardUsed({TorgalAFineHound.class, EliteVanguard.class, GrizzlyBears.class, HowlpackWolf.class,
        Eject.class, IronGiant.class, ItemShopkeep.class, RinoaHeartilly.class})
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

    @Test
    @DisplayName("A non-Human spell does not use up the first Human trigger")
    void nonHumanBeforeHumanStillAllowsCounters() {
        addReadyTorgal();
        IronGiant giant = new IronGiant();
        ItemShopkeep human = new ItemShopkeep();
        harness.setHand(player1, List.of(giant, human));
        harness.addMana(player1, ManaColor.RED, 9);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(giant).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(human).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Human cast before Torgal arrived still counts as the first Human")
    void earlierHumanBeforeTorgalPreventsTrigger() {
        ItemShopkeep first = new ItemShopkeep();
        ItemShopkeep second = new ItemShopkeep();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        addReadyTorgal();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(second).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent-controlled Wolves do not contribute counters")
    void countsOnlyControllerDogsAndWolves() {
        addReadyTorgal();
        harness.addToBattlefield(player2, new TorgalAFineHound());
        ItemShopkeep human = new ItemShopkeep();
        harness.setHand(player1, List.of(human));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(human).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An existing Dog contributes alongside Torgal")
    void countsExistingDogBeforeHumanEnters() {
        harness.enterBattlefieldAndReturn(player1, new RinoaHeartilly());
        resolveAllTriggers();
        addReadyTorgal();
        ItemShopkeep human = new ItemShopkeep();
        harness.setHand(player1, List.of(human));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(human).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rinoa's newly created Dog does not add a counter to Rinoa herself")
    void dogCreatedAfterEntryDoesNotCount() {
        addReadyTorgal();
        RinoaHeartilly human = new RinoaHeartilly();
        harness.setHand(player1, List.of(human));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(human).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Angelo")).isNotNull();
    }

    @Test
    @DisplayName("Dogs and Wolves are counted as the Human enters, after the trigger resolves")
    void recountsWolvesWhenHumanEnters() {
        Permanent torgal = addReadyTorgal();
        ItemShopkeep human = new ItemShopkeep();
        harness.setHand(player1, List.of(human, new Eject()));
        harness.setLibrary(player1, List.of(new IronGiant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, torgal.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(torgal);
        resolveAllTriggers();

        assertThat(findPermanent(human).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's Human spell neither gains counters nor consumes your first Human")
    void opponentHumanDoesNotTriggerOrConsumeControllerTrigger() {
        addReadyTorgal();
        ItemShopkeep opposingHuman = new ItemShopkeep();
        ItemShopkeep ownHuman = new ItemShopkeep();
        harness.setHand(player2, List.of(opposingHuman));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Item Shopkeep")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(ownHuman));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(ownHuman).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyTorgal() {
        return addCreatureReady(player1, new TorgalAFineHound());
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
