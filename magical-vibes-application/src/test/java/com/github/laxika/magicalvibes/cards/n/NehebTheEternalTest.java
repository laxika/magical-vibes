package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.l.LifeGoesOn;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NehebTheEternal.class, GrizzlyBears.class, Shock.class, Boomerang.class,
        LifeGoesOn.class, NicolBolasGodPharaoh.class})
class NehebTheEternalTest extends BaseCardTest {

    @Test
    @DisplayName("Afflict 3: becoming blocked makes the defending player lose 3 life")
    void blockedAfflictsDefender() {
        Permanent atk = harness.addToBattlefieldAndReturn(player1, new NehebTheEternal());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        atk.setAttackTarget(player2.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, new ArrayList<>());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Postcombat main: adds {R} equal to life opponents lost this turn")
    void postcombatMainAddsRedEqualToOpponentLifeLost() {
        harness.addToBattlefield(player1, new NehebTheEternal());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Clear leftover mana from casting so the pool is clean for the trigger.
        gd.playerManaPools.get(player1.getId()).clear();

        advanceToPostcombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    @DisplayName("Postcombat main: controller's own life loss does not produce mana")
    void ownLifeLossDoesNotCount() {
        harness.addToBattlefield(player1, new NehebTheEternal());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        gd.playerManaPools.get(player1.getId()).clear();

        advanceToPostcombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger on precombat main")
    void doesNotTriggerOnPrecombatMain() {
        harness.addToBattlefield(player1, new NehebTheEternal());
        gd.lifeLostThisTurn.put(player2.getId(), 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on an opponent's postcombat main")
    void doesNotTriggerOnOpponentsPostcombatMain() {
        harness.addToBattlefield(player1, new NehebTheEternal());
        gd.lifeLostThisTurn.put(player1.getId(), 5);

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleBlockersCauseOnlyOneAfflictTrigger() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NehebTheEternal());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    void lifeGainedDoesNotSubtractFromLifeLost() {
        harness.addToBattlefield(player1, new NehebTheEternal());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new LifeGoesOn()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0);
        harness.assertLife(player2, 22);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void countsLifeLostInResponseToPostcombatTrigger() {
        harness.addToBattlefield(player1, new NehebTheEternal());
        advanceToPostcombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void postcombatTriggerResolvesAfterNehebLeaves() {
        Permanent neheb = harness.addToBattlefieldAndReturn(player1, new NehebTheEternal());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        advanceToPostcombatMain(player1);
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, neheb.getId());
        harness.assertNotOnBattlefield(player1, "Neheb, the Eternal");

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerAfterNehebLeavesBeforePostcombatMain() {
        Permanent neheb = harness.addToBattlefieldAndReturn(player1, new NehebTheEternal());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, neheb.getId());

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    void afflictStillAffectsDefenderAfterAttackedPlaneswalkerLeaves() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NehebTheEternal());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(planeswalker.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, planeswalker.getId());
        harness.assertNotOnBattlefield(player2, "Nicol Bolas, God-Pharaoh");

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    private void advanceToPostcombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
