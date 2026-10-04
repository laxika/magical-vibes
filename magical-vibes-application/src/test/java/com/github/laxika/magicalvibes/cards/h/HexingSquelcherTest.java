package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HexingSquelcher.class, Counterspell.class, GrizzlyBears.class,
        IcyManipulator.class, MightOfOaks.class, Shock.class})
class HexingSquelcherTest extends BaseCardTest {

    @Test
    @DisplayName("The Hexing Squelcher spell cannot be countered")
    void spellCannotBeCountered() {
        HexingSquelcher squelcher = new HexingSquelcher();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(squelcher));
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, squelcher.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hexing Squelcher");
        harness.assertInGraveyard(player2, "Counterspell");
    }

    @Test
    @DisplayName("Spells you control cannot be countered")
    void protectsAllSpellsYouControl() {
        harness.addToBattlefield(player1, new HexingSquelcher());

        Shock shock = new Shock();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Counterspell");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Other creatures you control have Ward—Pay 2 life")
    void grantsWardToOtherCreatures() {
        harness.addToBattlefield(player1, new HexingSquelcher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Shock shock = new Shock();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability targeting another creature")
    void grantsWardAgainstTargetingAbilities() {
        harness.addToBattlefield(player1, new HexingSquelcher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator), null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying Ward's life cost lets the targeting spell resolve")
    void payingWardLifeCostLetsSpellResolve() {
        harness.addToBattlefield(player1, new HexingSquelcher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        MightOfOaks mightOfOaks = new MightOfOaks();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(mightOfOaks));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(9);
    }

    @Test
    void protectsCreatureSpellsAsWellAsInstants() {
        harness.addToBattlefield(player1, new HexingSquelcher());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Counterspell");
    }

    @Test
    void doesNotProtectOpponentsSpellsFromCountering() {
        harness.addToBattlefield(player1, new HexingSquelcher());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.setHand(player1, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Counterspell");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownWardRequiresExactlyOneLifePayment() {
        Permanent squelcher = harness.addToBattlefieldAndReturn(player1, new HexingSquelcher());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, squelcher.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Hexing Squelcher");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownWardCountersAnActivatedAbilityWhenPaymentIsDeclined() {
        Permanent squelcher = harness.addToBattlefieldAndReturn(player1, new HexingSquelcher());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, squelcher.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(squelcher.isTapped()).isFalse();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void friendlyTargetingDoesNotTriggerWard() {
        harness.addToBattlefield(player1, new HexingSquelcher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MightOfOaks()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(9);
    }

    @Test
    void wardIsNotGrantedToOpposingCreatures() {
        harness.addToBattlefield(player1, new HexingSquelcher());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void insufficientLifeCannotPayGrantedWard() {
        harness.addToBattlefield(player1, new HexingSquelcher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void twoSquelchersGrantTwoIndependentWardAbilities() {
        harness.addToBattlefield(player1, new HexingSquelcher());
        harness.addToBattlefield(player1, new HexingSquelcher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void alreadyTriggeredWardSurvivesTheGrantingSquelcherLeaving() {
        Permanent squelcher = harness.addToBattlefieldAndReturn(player1, new HexingSquelcher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, squelcher.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Hexing Squelcher");
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void leavingBattlefieldEndsGrantedWardAndSpellProtection() {
        Permanent squelcher = harness.addToBattlefieldAndReturn(player1, new HexingSquelcher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Shock protectedSpell = new Shock();
        harness.setHand(player1, List.of(protectedSpell));
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Counterspell()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, squelcher.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Hexing Squelcher");

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.castInstant(player2, 0, protectedSpell.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Counterspell");
        assertThat(gd.stack).isEmpty();
    }
}
