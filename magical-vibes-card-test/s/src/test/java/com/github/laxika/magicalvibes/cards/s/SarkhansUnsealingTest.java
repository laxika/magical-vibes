package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.Gigantosaurus;
import com.github.laxika.magicalvibes.cards.g.GoreclawTerrorOfQalSisma;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.h.HungeringHydra;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.PelakkaWurm;
import com.github.laxika.magicalvibes.cards.v.VigilantBaloth;
import com.github.laxika.magicalvibes.cards.v.VineMare;
import com.github.laxika.magicalvibes.cards.v.VivienReid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarkhansUnsealing.class, ColossalDreadmaw.class, GreenwoodSentinel.class,
        PelakkaWurm.class, VivienReid.class, Cancel.class, Gigantosaurus.class,
        GoreclawTerrorOfQalSisma.class, HungeringHydra.class, Naturalize.class,
        VigilantBaloth.class, VineMare.class})
class SarkhansUnsealingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature with power 4 through 6 deals 4 damage to any target")
    void mediumPowerCreatureDealsDamageToAnyTarget() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Casting a creature with power 7 or greater damages each opponent and their creatures and planeswalkers")
    void highPowerCreatureDealsMassDamage() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.setLife(player2, 20);
        Permanent opponentCreature = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent controllerCreature = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent opponentPlaneswalker = harness.addToBattlefieldAndReturn(player2, new VivienReid());
        opponentPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new PelakkaWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(controllerCreature.getMarkedDamage()).isZero();
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature with power less than 4 does not trigger")
    void lowPowerCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void powerFourTriggersBeforeCreatureResolves() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.setHand(player1, List.of(new GoreclawTerrorOfQalSisma()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player1, "Goreclaw, Terror of Qal Sisma");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void powerFiveCanDamageControllersCreature() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new VigilantBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void mediumPowerCanDamagePlaneswalker() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VivienReid());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void mediumPowerCanDamageController() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    void powerAboveSevenDamagesHexproofCreatureButNotControllersPermanents() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.addToBattlefield(player2, new VineMare());
        Permanent friendlyPlaneswalker = harness.addToBattlefieldAndReturn(player1, new VivienReid());
        friendlyPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Gigantosaurus()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Vine Mare");
        assertThat(friendlyPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "Gigantosaurus");
    }

    @Test
    void opponentsCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Gigantosaurus()));
        harness.addMana(player2, ManaColor.GREEN, 5);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void castingNoncreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.setHand(player1, List.of(new SarkhansUnsealing()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    void countersOnEnteringCreatureDoNotCountAsSpellPower() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.setHand(player1, List.of(new HungeringHydra()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0, 7);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Hungering Hydra");
    }

    @Test
    void triggerStillResolvesWhenCreatureSpellIsCountered() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        ColossalDreadmaw creature = new ColossalDreadmaw();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Colossal Dreadmaw");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player1, "Colossal Dreadmaw");
    }

    @Test
    void triggerStillDealsDamageAfterEnchantmentIsDestroyed() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SarkhansUnsealing());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Sarkhan's Unsealing");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }
}
