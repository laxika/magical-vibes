package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetStaticaster;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SluicewayScorpion;
import com.github.laxika.magicalvibes.cards.v.VolcanicGeyser;
import com.github.laxika.magicalvibes.cards.v.VraskaTheUnseen;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PalisadeGiant.class, GrizzlyBears.class, Shock.class, VolcanicGeyser.class,
        IzzetStaticaster.class, SluicewayScorpion.class, VraskaTheUnseen.class,
        Combust.class, ChandraNalaar.class})
class PalisadeGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Damage that would be dealt to you is dealt to the Giant instead")
    void damageToControllerRedirectedToGiant() {
        harness.addToBattlefield(player2, new PalisadeGiant());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Palisade Giant");
    }

    @Test
    @DisplayName("Damage that would be dealt to another permanent you control is dealt to the Giant instead")
    void damageToOtherPermanentRedirectedToGiant() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new PalisadeGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isEqualTo(0);
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage dealt to the Giant itself is not redirected")
    void damageToGiantIsNotRedirected() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new PalisadeGiant());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, giant.getId());

        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Palisade Giant");
    }

    @Test
    @DisplayName("An opponent's permanents are unaffected by the redirect")
    void opponentPermanentsAreUnaffected() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new PalisadeGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        assertThat(giant.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Enough redirected damage destroys the Giant, sparing its controller")
    void lethalRedirectedDamageDestroysGiant() {
        harness.addToBattlefield(player2, new PalisadeGiant());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.addMana(player1, ManaColor.RED, 11);

        // 7 damage aimed at the controller is redirected to the 2/7 Giant, destroying it.
        harness.castInstant(player1, 0, 7, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Palisade Giant");
    }

    @Test
    @DisplayName("Combat damage to another creature retains deathtouch after redirection")
    void redirectedBlockedCombatDamageRetainsDeathtouch() {
        harness.addToBattlefield(player2, new PalisadeGiant());
        addCreatureReady(player2, new IzzetStaticaster());
        addCreatureReady(player1, new SluicewayScorpion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        harness.assertInGraveyard(player2, "Palisade Giant");
        harness.assertOnBattlefield(player2, "Izzet Staticaster");
        harness.assertOnBattlefield(player1, "Sluiceway Scorpion");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unblocked deathtouch damage is redirected and destroys the Giant")
    void redirectedUnblockedCombatDamageRetainsDeathtouch() {
        harness.addToBattlefield(player2, new PalisadeGiant());
        addCreatureReady(player1, new SluicewayScorpion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        harness.assertInGraveyard(player2, "Palisade Giant");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Combat damage to a controlled planeswalker is redirected")
    void combatDamageToPlaneswalkerIsRedirected() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new PalisadeGiant());
        Permanent vraska = harness.addToBattlefieldAndReturn(player2, new VraskaTheUnseen());
        vraska.setCounterCount(CounterType.LOYALTY, 5);
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, vraska.getId())));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Spell damage to a controlled planeswalker is redirected")
    void spellDamageToPlaneswalkerIsRedirected() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new PalisadeGiant());
        Permanent vraska = harness.addToBattlefieldAndReturn(player2, new VraskaTheUnseen());
        vraska.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, vraska.getId());

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Unpreventable damage to another creature is still redirected")
    void unpreventableDamageIsRedirected() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new PalisadeGiant());
        Permanent staticaster = harness.addToBattlefieldAndReturn(player2, new IzzetStaticaster());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, staticaster.getId());

        harness.assertOnBattlefield(player2, "Izzet Staticaster");
        assertThat(staticaster.getMarkedDamage()).isZero();
        assertThat(giant.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("The controller chooses the redirection order when controlling two Giants")
    void multipleGiantsRequireControllerChoice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new PalisadeGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PalisadeGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lethal redirected damage does not remove the Giant during a simultaneous damage event")
    void lethalRedirectionStillProtectsOtherCreaturesDuringResolution() {
        harness.addToBattlefield(player2, new PalisadeGiant());
        Permanent staticaster = harness.addToBattlefieldAndReturn(player2, new IzzetStaticaster());
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Palisade Giant");
        harness.assertOnBattlefield(player2, "Izzet Staticaster");
        assertThat(staticaster.getMarkedDamage()).isZero();
    }
}
