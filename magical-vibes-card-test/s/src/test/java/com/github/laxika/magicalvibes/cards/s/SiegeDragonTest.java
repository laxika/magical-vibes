package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraPyromaster;
import com.github.laxika.magicalvibes.cards.c.Chronostutter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SiegeDragon.class, AirElemental.class, GrizzlyBears.class, WallOfWood.class,
        ChandraPyromaster.class, Chronostutter.class})
class SiegeDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys the opponent's Walls")
    void etbDestroysOpponentWalls() {
        harness.addToBattlefield(player2, new WallOfWood());

        castDragon();

        harness.assertNotOnBattlefield(player2, "Wall of Wood");
        harness.assertInGraveyard(player2, "Wall of Wood");
    }

    @Test
    @DisplayName("ETB spares the controller's own Walls and non-Wall creatures")
    void etbSparesOwnWallsAndOtherCreatures() {
        harness.addToBattlefield(player1, new WallOfWood());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDragon();

        harness.assertOnBattlefield(player1, "Wall of Wood");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking with no Walls on defense deals 2 damage to each non-flying creature the defender controls")
    void attackDamagesNonFliersWhenDefenderHasNoWalls() {
        addCreatureReady(player1, new SiegeDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fliers the defending player controls take no damage")
    void attackSparesDefendingFliers() {
        addCreatureReady(player1, new SiegeDragon());
        harness.addToBattlefield(player2, new AirElemental());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The attacker's own creatures take no damage")
    void attackSparesOwnCreatures() {
        addCreatureReady(player1, new SiegeDragon());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ownBears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No damage when the defending player controls a Wall")
    void noAttackDamageWhenDefenderControlsWall() {
        addCreatureReady(player1, new SiegeDragon());
        harness.addToBattlefield(player2, new WallOfWood());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Wall of Wood");
    }

    @Test
    @DisplayName("A Wall entering before the attack trigger resolves prevents the damage")
    void wallEnteringInResponsePreventsDamage() {
        addCreatureReady(player1, new SiegeDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player2, new WallOfWood());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Wall of Wood").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An existing Wall prevents the attack ability from triggering")
    void wallPreventsTriggerFromBeingPutOnStack() {
        addCreatureReady(player1, new SiegeDragon());
        harness.addToBattlefield(player2, new WallOfWood());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger does not damage noncreature planeswalkers")
    void attackSparesNoncreaturePlaneswalkers() {
        addCreatureReady(player1, new SiegeDragon());
        harness.addToBattlefield(player2, new ChandraPyromaster());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent chandra = findPermanent(player2, "Chandra, Pyromaster");
        int loyaltyBefore = chandra.getCounterCount(CounterType.LOYALTY);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The attack trigger still damages the defender's creatures after the Dragon leaves")
    void attackTriggerResolvesAfterDragonLeaves() {
        Permanent dragon = addCreatureReady(player1, new SiegeDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Chronostutter()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        declareAttackers(player1, List.of(0));
        harness.castAndResolveInstant(player2, 0, dragon.getId());
        harness.assertNotOnBattlefield(player1, "Siege Dragon");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castDragon() {
        harness.setHand(player1, List.of(new SiegeDragon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        resolveAllTriggers();
    }
}
