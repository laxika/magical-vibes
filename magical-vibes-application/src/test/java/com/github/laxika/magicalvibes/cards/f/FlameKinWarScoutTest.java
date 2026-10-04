package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.s.SilkwingScout;
import com.github.laxika.magicalvibes.cards.s.SimicSkySwallower;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameKinWarScout.class, SilkwingScout.class, SimicSkySwallower.class, AzoriusSignet.class})
class FlameKinWarScoutTest extends BaseCardTest {

    @Test
    void anotherCreatureEnteringSacrificesScoutAndDealsDamageToIt() {
        harness.addToBattlefield(player1, new FlameKinWarScout());
        harness.castFromHand(player1, new SilkwingScout(), "{2}{U}");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Flame-Kin War Scout");
        harness.assertInGraveyard(player1, "Silkwing Scout");
    }

    @Test
    void enteringCreatureSurvivesWithFourMarkedDamage() {
        harness.addToBattlefield(player1, new FlameKinWarScout());
        harness.castFromHand(player1, new SimicSkySwallower(), "{5}{G}{U}");
        resolveAllTriggers();

        Permanent skySwallower = findPermanent(player1, "Simic Sky Swallower");
        assertThat(skySwallower.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Flame-Kin War Scout");
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        harness.castFromHand(player1, new FlameKinWarScout(), "{3}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Flame-Kin War Scout");
    }

    @Test
    void doesNotTriggerForAnotherPermanentEntering() {
        harness.addToBattlefield(player1, new FlameKinWarScout());
        harness.castFromHand(player1, new AzoriusSignet(), "{2}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Flame-Kin War Scout");
        harness.assertOnBattlefield(player1, "Azorius Signet");
    }

    @Test
    void anotherPlayersCreatureEnteringAlsoTriggers() {
        harness.addToBattlefield(player1, new FlameKinWarScout());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new SilkwingScout(), "{2}{U}");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Flame-Kin War Scout");
        harness.assertInGraveyard(player2, "Silkwing Scout");
    }

    @Test
    void laterTriggerCannotDealDamageAfterScoutHasAlreadyBeenSacrificed() {
        harness.addToBattlefield(player1, new FlameKinWarScout());
        Permanent skySwallower = harness.enterBattlefieldAndReturn(player1, new SimicSkySwallower());
        harness.enterBattlefieldAndReturn(player1, new SilkwingScout());

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Flame-Kin War Scout");
        harness.assertInGraveyard(player1, "Silkwing Scout");
        harness.assertOnBattlefield(player1, "Simic Sky Swallower");
        assertThat(skySwallower.getMarkedDamage()).isZero();
    }

    @Test
    void stillSacrificesScoutWhenEnteringCreatureLeavesBeforeResolution() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new FlameKinWarScout());
        harness.enterBattlefieldAndReturn(player1, new SilkwingScout());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, null);

        harness.assertInGraveyard(player1, "Silkwing Scout");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Flame-Kin War Scout");
    }
}
