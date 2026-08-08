package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpalEyeKondasYojimboTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the tap ability prompts for a damage source choice")
    void tapAbilityPromptsForSourceChoice() {
        Permanent opalEye = addReadyPermanent(player1, new OpalEyeKondasYojimbo());
        addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, opalEye), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Noncombat damage the chosen source would deal to a player is dealt to Opal-Eye instead")
    void redirectsNoncombatPlayerDamageToSelf() {
        harness.setLife(player2, 20);
        Permanent opalEye = addReadyPermanent(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, opalEye), 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(opalEye.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.sourceNextDamageRedirectToPermanentShields).isEmpty();
    }

    @Test
    @DisplayName("Noncombat damage the chosen source would deal to a creature is dealt to Opal-Eye instead")
    void redirectsNoncombatCreatureDamageToSelf() {
        Permanent opalEye = addReadyPermanent(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent victim = addReadyStats(player2, 3, 3);

        harness.activateAbility(player1, indexOf(player1, opalEye), 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(0);
        assertThat(opalEye.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage the chosen attacker would deal to the controller is dealt to Opal-Eye instead")
    void redirectsCombatDamageToSelf() {
        harness.setLife(player1, 20);
        Permanent opalEye = addReadyPermanent(player1, new OpalEyeKondasYojimbo());
        Permanent attacker = addReadyStats(player2, 3, 3);

        harness.activateAbility(player1, indexOf(player1, opalEye), 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(opalEye.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage from a source other than the chosen one is not redirected")
    void doesNotRedirectOtherSources() {
        harness.setLife(player2, 20);
        Permanent opalEye = addReadyPermanent(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent decoy = addReadyStats(player1, 2, 2);

        harness.activateAbility(player1, indexOf(player1, opalEye), 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, decoy.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(opalEye.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.sourceNextDamageRedirectToPermanentShields).isNotEmpty();
    }

    @Test
    @DisplayName("Only the next damage event from the chosen source is redirected")
    void onlyNextDamageEventIsRedirected() {
        harness.setLife(player2, 20);
        Permanent opalEye = addReadyPermanent(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, opalEye), 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 20);

        pyromancer.untap();
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(opalEye.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent opalEye = addReadyPermanent(player1, new OpalEyeKondasYojimbo());
        addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, opalEye), 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, gd.playerBattlefields.get(player2.getId()).getFirst().getId());

        assertThat(gd.sourceNextDamageRedirectToPermanentShields).isNotEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceNextDamageRedirectToPermanentShields).isEmpty();
    }

    @Test
    @DisplayName("The {1}{W} ability prevents the next 1 damage dealt to Opal-Eye")
    void preventsNextDamageToSelf() {
        Permanent opalEye = addReadyPermanent(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, opalEye), 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, opalEye.getId());
        harness.passBothPriorities();

        assertThat(opalEye.getMarkedDamage()).isEqualTo(0);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
