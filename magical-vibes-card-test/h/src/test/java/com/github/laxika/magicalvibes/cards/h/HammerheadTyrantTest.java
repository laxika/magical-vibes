package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeScout;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HammerheadTyrant.class, GrizzlyBears.class, Island.class, Opt.class, SakuraTribeScout.class, SolRing.class})
class HammerheadTyrantTest extends BaseCardTest {

    @Test
    void returnsAnOpponentPermanentWithManaValueAtMostTheCastSpell() {
        harness.addToBattlefield(player1, new HammerheadTyrant());
        Permanent eligible = addCreatureReady(player2, new SakuraTribeScout());
        Permanent tooExpensive = addCreatureReady(player2, new GrizzlyBears());
        castOpt();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(eligible.getId()).doesNotContain(tooExpensive.getId());

        harness.handlePermanentChosen(player1, eligible.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Sakura-Tribe Scout");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void mayChooseNoTargetWhenNoPermanentQualifies() {
        harness.addToBattlefield(player1, new HammerheadTyrant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castOpt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void returnsNoncreaturePermanentAndExcludesOwnPermanents() {
        harness.addToBattlefield(player1, new HammerheadTyrant());
        Permanent ownRing = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent opposingRing = harness.addToBattlefieldAndReturn(player2, new SolRing());
        castOpt();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opposingRing.getId()).doesNotContain(ownRing.getId());
        harness.handlePermanentChosen(player1, opposingRing.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Sol Ring");
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    void mayDeclineEvenWhenAnEligibleTargetExists() {
        harness.addToBattlefield(player1, new HammerheadTyrant());
        harness.addToBattlefield(player2, new SolRing());
        castOpt();

        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Sol Ring");
        harness.assertNotInHand(player2, "Sol Ring");
    }

    @Test
    void doesNotTriggerForAnOpponentsSpell() {
        harness.addToBattlefield(player1, new HammerheadTyrant());
        harness.addToBattlefield(player1, new SolRing());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Opt(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    void returnsStolenPermanentToItsOwnerRatherThanItsController() {
        harness.addToBattlefield(player1, new HammerheadTyrant());
        SolRing ring = new SolRing();
        ring.setOwnerId(player1.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, ring);
        castOpt();

        harness.handlePermanentChosen(player1, stolen.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Sol Ring");
        harness.assertNotInHand(player2, "Sol Ring");
        harness.assertNotOnBattlefield(player2, "Sol Ring");
    }

    @Test
    void creatureSpellTriggersBeforeThatSpellResolves() {
        harness.addToBattlefield(player1, new HammerheadTyrant());
        Permanent opposingTyrant = harness.addToBattlefieldAndReturn(player2, new HammerheadTyrant());
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new HammerheadTyrant(), "{4}{U}{U}");

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opposingTyrant.getId());
        assertThat(countPermanents(player1, "Hammerhead Tyrant")).isEqualTo(1);
        harness.handlePermanentChosen(player1, opposingTyrant.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Hammerhead Tyrant");
        assertThat(countPermanents(player1, "Hammerhead Tyrant")).isEqualTo(2);
    }

    private void castOpt() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();
    }

    @Test
    void cannotTargetALandEvenThoughItsManaValueIsZero() {
        harness.addToBattlefield(player1, new HammerheadTyrant());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        castOpt();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ring.getId()).doesNotContain(land.getId());
        harness.handlePermanentChosen(player1, ring.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Sol Ring");
    }
}
