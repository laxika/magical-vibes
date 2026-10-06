package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarenbrigSquire;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OkoThiefOfCrowns;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedcapMelee.class, GrizzlyBears.class, Mountain.class, RagingRedcap.class,
        GarenbrigSquire.class, OkoThiefOfCrowns.class})
class RedcapMeleeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to a nonred creature and sacrifices a land")
    void dealsDamageToNonredCreatureAndSacrificesLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not sacrifice a land when the damaged creature is red")
    void doesNotSacrificeLandForRedCreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new RagingRedcap());
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Raging Redcap"));

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player2, "Raging Redcap");
    }

    @Test
    @DisplayName("Rejects a noncreature, nonplaneswalker target")
    void rejectsInvalidTarget() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Mountain")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damagesNonredPlaneswalkerAndSacrificesLand() {
        harness.addToBattlefield(player1, new Mountain());
        var oko = harness.addToBattlefieldAndReturn(player2, new OkoThiefOfCrowns());
        oko.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, oko.getId());

        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Oko, Thief of Crowns");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    void fullyPreventedDamageDoesNotRequireSacrifice() {
        harness.addToBattlefield(player1, new Mountain());
        var creature = harness.addToBattlefieldAndReturn(player2, new GarenbrigSquire());
        creature.setDamagePreventionShield(4);
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Garenbrig Squire");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    void partiallyPreventedDamageStillRequiresSacrifice() {
        harness.addToBattlefield(player1, new Mountain());
        var creature = harness.addToBattlefieldAndReturn(player2, new GarenbrigSquire());
        creature.setDamagePreventionShield(3);
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Garenbrig Squire");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    void stillDealsDamageWhenControllerHasNoLand() {
        harness.addToBattlefield(player2, new GarenbrigSquire());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Garenbrig Squire"));

        harness.assertInGraveyard(player2, "Garenbrig Squire");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player1, "Redcap Melee");
    }

    @Test
    void controllerChoosesExactlyOneOfTheirLandsToSacrifice() {
        var firstLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        var secondLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GarenbrigSquire());
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Garenbrig Squire"));
        harness.handleMultiplePermanentsChosen(player1, java.util.List.of(secondLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstLand).doesNotContain(secondLand);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Garenbrig Squire");
    }

    @Test
    void canDamageOwnNonredCreatureAndStillRequiresSacrifice() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new GarenbrigSquire());
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Garenbrig Squire"));

        harness.assertInGraveyard(player1, "Garenbrig Squire");
        harness.assertInGraveyard(player1, "Mountain");
    }
}
