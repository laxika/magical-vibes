package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.cards.s.SoratamiCloudskater;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Earthshaker.class, DesperateRitual.class, DevotedRetainer.class, HarshDeceiver.class,
        SoratamiCloudskater.class})
class EarthshakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell deals 2 damage to each creature without flying")
    void arcaneSpellDamagesGroundCreatures() {
        addCreatureReady(player1, new Earthshaker());
        addCreatureReady(player2, new DevotedRetainer());
        Permanent flyer = addCreatureReady(player2, new SoratamiCloudskater());
        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Devoted Retainer");
        harness.assertOnBattlefield(player2, "Soratami Cloudskater");
        assertThat(flyer.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Casting a Spirit spell triggers the damage")
    void spiritSpellDamagesGroundCreatures() {
        addCreatureReady(player1, new Earthshaker());
        addCreatureReady(player2, new DevotedRetainer());
        harness.castFromHand(player1, new HarshDeceiver(), "{3}{W}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Devoted Retainer");
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger the damage")
    void unrelatedSpellDoesNotTrigger() {
        addCreatureReady(player1, new Earthshaker());
        Permanent ground = addCreatureReady(player2, new DevotedRetainer());
        harness.castFromHand(player1, new DevotedRetainer(), "{W}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Devoted Retainer");
        assertThat(ground.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An opponent casting a Spirit does not trigger Earthshaker")
    void opponentSpiritSpellDoesNotTrigger() {
        Permanent earthshaker = addCreatureReady(player1, new Earthshaker());
        Permanent ground = addCreatureReady(player2, new DevotedRetainer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new HarshDeceiver(), "{3}{W}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Devoted Retainer");
        assertThat(earthshaker.getMarkedDamage()).isZero();
        assertThat(ground.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Earthshaker damages itself since it has no flying")
    void earthshakerDamagesItself() {
        Permanent earthshaker = addCreatureReady(player1, new Earthshaker());
        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");

        harness.passBothPriorities();

        assertThat(earthshaker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The damage includes friendly ground creatures but neither player")
    void damagesFriendlyCreaturesWithoutDamagingPlayers() {
        addCreatureReady(player1, new Earthshaker());
        addCreatureReady(player1, new DevotedRetainer());
        Permanent friendlyFlyer = addCreatureReady(player1, new SoratamiCloudskater());
        Permanent opposingGround = addCreatureReady(player2, new HarshDeceiver());
        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devoted Retainer");
        assertThat(friendlyFlyer.getMarkedDamage()).isZero();
        assertThat(opposingGround.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The cast trigger resolves before the Spirit enters the battlefield")
    void triggeringSpiritIsNotDamagedByItsOwnCastTrigger() {
        Permanent earthshaker = addCreatureReady(player1, new Earthshaker());
        harness.castFromHand(player1, new HarshDeceiver(), "{3}{W}");

        harness.passBothPriorities();

        assertThat(earthshaker.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Harsh Deceiver");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Harsh Deceiver");
        assertThat(findPermanent(player1, "Harsh Deceiver").getMarkedDamage()).isZero();
        assertThat(earthshaker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Earthshaker does not trigger when it is itself cast")
    void doesNotTriggerOnItsOwnCast() {
        Permanent ground = addCreatureReady(player2, new DevotedRetainer());
        harness.castFromHand(player1, new Earthshaker(), "{4}{R}{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Earthshaker");
        harness.assertOnBattlefield(player2, "Devoted Retainer");
        assertThat(ground.getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Earthshaker").getMarkedDamage()).isZero();
    }
}
