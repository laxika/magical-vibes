package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GideonsIntervention.class, GrizzlyBears.class, HillGiant.class, Shock.class, Humility.class, Opalescence.class})
class GideonsInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Gideon's Intervention awaits a card name choice and records it")
    void resolvingChoosesCardName() {
        harness.setHand(player1, List.of(new GideonsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        Permanent gi = findPermanent(player1, "Gideon's Intervention");
        assertThat(gi.getChosenName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Opponents can't cast spells with the chosen name")
    void opponentCannotCastChosenName() {
        addReadyIntervention(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The controller can still cast spells with the chosen name")
    void controllerCanStillCastChosenName() {
        addReadyIntervention(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage to you from a source with the chosen name is prevented")
    void combatDamageToControllerPrevented() {
        addReadyIntervention(player1, "Grizzly Bears");
        harness.setLife(player1, 20);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Noncombat damage to you from a source with the chosen name is prevented (even your own)")
    void noncombatDamageToControllerPrevented() {
        addReadyIntervention(player1, "Shock");
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Noncombat damage to a permanent you control from a source with the chosen name is prevented")
    void noncombatDamageToControllersPermanentPrevented() {
        addReadyIntervention(player1, "Shock");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage to a creature you control from a source with the chosen name is prevented")
    void combatDamageToControllersCreaturePrevented() {
        addReadyIntervention(player1, "Grizzly Bears");

        // Attacker is at index 0 of the attacking player's battlefield.
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // The named attacker's combat damage to the Hill Giant is prevented; the Hill Giant survives unmarked.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(blocker.getId()));
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage from a source with a different name is not prevented")
    void differentNamedSourceStillDealsDamage() {
        addReadyIntervention(player1, "Hill Giant");
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Losing all abilities disables prevention of damage to the controller")
    void abilityLossDisablesPlayerDamagePrevention() {
        addReadyIntervention(player1, "Shock");
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player2, new Humility());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Losing all abilities disables prevention of damage to controlled permanents")
    void abilityLossDisablesPermanentDamagePrevention() {
        addReadyIntervention(player1, "Shock");
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player2, new Humility());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, giant.getId());

        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Damage to an opponent is not prevented even when you control the named source")
    void namedSourceStillDamagesOpponent() {
        addReadyIntervention(player1, "Shock");
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Removing the enchantment ends its casting restriction")
    void removalEndsCastingRestriction() {
        Permanent intervention = addReadyIntervention(player1, "Grizzly Bears");
        gd.playerBattlefields.get(player1.getId()).remove(intervention);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A token-only name cannot be chosen")
    void tokenOnlyNameCannotBeChosen() {
        harness.setHand(player1, List.of(new GideonsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Zombie Token"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Permanent addReadyIntervention(Player player, String chosenName) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GideonsIntervention());
        perm.setChosenName(chosenName);
        return perm;
    }
}
