package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RadjanSpirit;
import com.github.laxika.magicalvibes.cards.s.StarlitAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuminousBroodmoth.class, GrizzlyBears.class, StarlitAngel.class, RadjanSpirit.class})
class LuminousBroodmothTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature without flying with a flying counter")
    void returnsCreatureWithoutFlyingWithFlyingCounter() {
        harness.addToBattlefield(player1, new LuminousBroodmoth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        kill(creature);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return a creature that had flying")
    void doesNotReturnCreatureWithFlying() {
        harness.addToBattlefield(player1, new LuminousBroodmoth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StarlitAngel());

        kill(creature);

        harness.assertNotOnBattlefield(player1, "Starlit Angel");
        harness.assertInGraveyard(player1, "Starlit Angel");
    }

    @Test
    @CardUsed({LuminousBroodmoth.class, RadjanSpirit.class})
    @DisplayName("Returns itself when it dies without flying")
    void returnsItselfAfterLosingFlying() {
        addCreatureReady(player1, new RadjanSpirit());
        Permanent broodmoth = harness.addToBattlefieldAndReturn(player1, new LuminousBroodmoth());
        harness.activateAbility(player1, 0, null, broodmoth.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, broodmoth, Keyword.FLYING)).isFalse();

        kill(broodmoth);

        Permanent returned = findPermanent(player1, "Luminous Broodmoth");
        assertThat(returned.getId()).isNotEqualTo(broodmoth.getId());
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Luminous Broodmoth");
    }

    @Test
    @DisplayName("A returned creature does not return again while it has flying")
    void doesNotReturnCreatureAgainWithFlyingCounter() {
        harness.addToBattlefield(player1, new LuminousBroodmoth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        kill(creature);
        Permanent returned = findPermanent(player1, "Grizzly Bears");

        kill(returned);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns each eligible creature when Broodmoth dies simultaneously")
    void returnsCreaturesThatDieWithBroodmoth() {
        Permanent broodmoth = harness.addToBattlefieldAndReturn(player1, new LuminousBroodmoth());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        broodmoth.setMarkedDamage(4);
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.FLYING)).isEqualTo(1));
        harness.assertInGraveyard(player1, "Luminous Broodmoth");
        harness.assertNotOnBattlefield(player1, "Luminous Broodmoth");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return an opponent's creature")
    void doesNotReturnOpponentsCreature() {
        harness.addToBattlefield(player1, new LuminousBroodmoth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        kill(creature);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner rather than its former controller")
    void returnsStolenCreatureToOwner() {
        harness.addToBattlefield(player1, new LuminousBroodmoth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(creature.getId(), player2.getId());

        kill(creature);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return a card removed from the graveyard before resolution")
    void doesNotReturnCardThatLeftGraveyard() {
        harness.addToBattlefield(player1, new LuminousBroodmoth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        gd.playerGraveyards.get(player1.getId()).remove(creature.getCard());
        gd.addToExile(player1.getId(), creature.getCard());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Multiple Broodmoths return the same card only once")
    void multipleBroodmothsDoNotDuplicateReturn() {
        harness.addToBattlefield(player1, new LuminousBroodmoth());
        harness.addToBattlefield(player1, new LuminousBroodmoth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        kill(creature);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.FLYING)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    private void kill(Permanent creature) {
        creature.setMarkedDamage(creature.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
