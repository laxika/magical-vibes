package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.h.HavocDevils;
import com.github.laxika.magicalvibes.cards.m.MyrReservoir;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IsarethTheAwakener.class, GreenwoodSentinel.class, ChildOfNight.class,
        HavocDevils.class, Shock.class, Disperse.class, Ornithopter.class, MyrReservoir.class})
class IsarethTheAwakenerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {X} returns a creature card with mana value X with a corpse counter")
    void payingReturnsMatchingCreature() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve the attack trigger -> prompts for X
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities(); // resolve the reflexive return trigger

        Permanent returned = findPermanent(player1, "Greenwood Sentinel");
        assertThat(returned.getCounterCount(CounterType.CORPSE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("A returned creature is exiled instead of going to the graveyard when it dies")
    void returnedCreatureIsExiledInsteadOfDying() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Greenwood Sentinel");
        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.assertNotInGraveyard(player1, "Greenwood Sentinel");
        assertThat(gd.exiledCards)
                .extracting(entry -> entry.card().getName())
                .contains("Greenwood Sentinel");
    }

    @Test
    @DisplayName("With several matching creature cards the controller chooses which one returns")
    void controllerChoosesAmongMatchingCreatures() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel(), new ChildOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleGraveyardCardChosen(player1, 1); // Child of Night
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Child of Night")).isNotNull();
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Choosing X=0 with no matching creature returns nothing")
    void zeroWithNoMatchingCreatureReturnsNothing() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        assertThat(countPermanents(player1, "Greenwood Sentinel")).isZero();
    }

    @Test
    @DisplayName("No creature card with the paid mana value means nothing is returned")
    void noMatchingManaValueReturnsNothing() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new HavocDevils())); // mana value 4
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        harness.assertInGraveyard(player1, "Havoc Devils");
        assertThat(countPermanents(player1, "Havoc Devils")).isZero();
    }

    @Test
    @DisplayName("Paying zero returns a creature with mana value zero")
    void payingZeroReturnsZeroManaValueCreature() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(findPermanent(player1, "Ornithopter").getCounterCount(CounterType.CORPSE))
                .isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("The controller can pay zero even with no mana available")
    void payingZeroRequiresNoManaSources() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new Ornithopter()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleXValueChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Mana restricted to Myr cannot pay Isareth's triggered ability")
    void myrRestrictedManaCannotPayForReturn() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.addToBattlefield(player1, new MyrReservoir());
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        declareAttackers(player1, List.of(0));
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 2))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleXValueChosen(player1, 0);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Removing the corpse counter does not stop exile when the creature is bounced")
    void bounceStillExilesAfterCorpseCounterIsRemoved() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Greenwood Sentinel");
        returned.setCounterCount(CounterType.CORPSE, 0);
        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertNotInHand(player1, "Greenwood Sentinel");
        harness.assertNotInGraveyard(player1, "Greenwood Sentinel");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .contains("Greenwood Sentinel");
    }

    @Test
    @DisplayName("Removing Isareth in response does not stop the return or its exile replacement")
    void returnAndExilePersistAfterIsarethLeaves() {
        Permanent isareth = addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new Disperse(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.castAndResolveInstant(player1, 0, isareth.getId());
        harness.assertInHand(player1, "Isareth the Awakener");
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Greenwood Sentinel");
        assertThat(returned.isAttacking()).isFalse();
        assertThat(returned.getCounterCount(CounterType.CORPSE)).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertNotInGraveyard(player1, "Greenwood Sentinel");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .contains("Greenwood Sentinel");
    }
}
