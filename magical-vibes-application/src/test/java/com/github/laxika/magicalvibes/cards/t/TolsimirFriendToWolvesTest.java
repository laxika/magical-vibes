package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArlinnsWolf;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TolsimirFriendToWolves.class, GrizzlyBears.class, HillGiant.class,
        ArlinnsWolf.class, TotallyLost.class, MaskwoodNexus.class})
class TolsimirFriendToWolvesTest extends BaseCardTest {

    @Test
    void enteringCreatesLegendaryVojaAndGainsLife() {
        castTolsimir();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent voja = findPermanent(player1, "Voja, Friend to Elves");
        assertThat(voja.getCard().isToken()).isTrue();
        assertThat(voja.getCard().getPower()).isEqualTo(3);
        assertThat(voja.getCard().getToughness()).isEqualTo(3);
        assertThat(voja.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(voja.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(voja.getCard().getSupertypes()).containsExactly(CardSupertype.LEGENDARY);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void wolfEntryGainsLifeAndFightsOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castTolsimir();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Voja, Friend to Elves");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void nonWolfEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new TolsimirFriendToWolves());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void mayDeclineFightEvenWhenOpponentControlsCreature() {
        harness.addToBattlefield(player1, new TolsimirFriendToWolves());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent wolf = harness.enterBattlefieldAndReturn(player1, new ArlinnsWolf());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(wolf.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Arlinn's Wolf");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void opponentWolfEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new TolsimirFriendToWolves());
        harness.enterBattlefieldAndReturn(player2, new ArlinnsWolf());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void gainsLifeWithoutFightWhenEnteringWolfLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new TolsimirFriendToWolves());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent wolf = harness.enterBattlefieldAndReturn(player1, new ArlinnsWolf());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        putOnTopOfLibrary(wolf);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(opponentCreature.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Arlinn's Wolf");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void doesNotGainLifeWhenChosenFightTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new TolsimirFriendToWolves());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent wolf = harness.enterBattlefieldAndReturn(player1, new ArlinnsWolf());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        putOnTopOfLibrary(opponentCreature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(wolf.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Arlinn's Wolf");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void createsVojaWithoutLifeGainWhenTolsimirLeavesBeforeTokenCreation() {
        castTolsimir();
        harness.passBothPriorities();

        putOnTopOfLibrary(findPermanent(player1, "Tolsimir, Friend to Wolves"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Voja, Friend to Elves");
        harness.assertNotOnBattlefield(player1, "Tolsimir, Friend to Wolves");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void wolfTriggerStillResolvesAfterTolsimirLeaves() {
        Permanent tolsimir = harness.addToBattlefieldAndReturn(player1, new TolsimirFriendToWolves());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.enterBattlefieldAndReturn(player1, new ArlinnsWolf());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        putOnTopOfLibrary(tolsimir);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertInGraveyard(player1, "Arlinn's Wolf");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void tolsimirTriggersForItsOwnEntryWhenItIsAWolf() {
        harness.addToBattlefield(player1, new MaskwoodNexus());
        castTolsimir();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
        harness.assertOnBattlefield(player1, "Voja, Friend to Elves");
    }

    private void putOnTopOfLibrary(Permanent permanent) {
        harness.setHand(player1, List.of(new TotallyLost()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, permanent.getId());
        harness.passBothPriorities();
    }

    private void castTolsimir() {
        harness.castFromHand(player1, new TolsimirFriendToWolves(), "{2}{G}{G}{W}");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
