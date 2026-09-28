package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OpalEyeKondasYojimbo.class, FirstVolley.class, GrizzlyBears.class, ProdigalPyromancer.class})
class OpalEyeKondasYojimboTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents Opal-Eye from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new OpalEyeKondasYojimbo());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Bushido gives Opal-Eye +1/+1 when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
        opalEye.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opalEye)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opalEye)).isEqualTo(5);
    }

    @Test
    @DisplayName("Bushido gives Opal-Eye +1/+1 when it blocks")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent opalEye = addCreatureReady(player2, new OpalEyeKondasYojimbo());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opalEye)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opalEye)).isEqualTo(5);
    }

    @Test
    @DisplayName("Bushido does not trigger when Opal-Eye is unblocked")
    void unblockedGetsNoBushidoBonus() {
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
        opalEye.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gqs.getEffectivePower(gd, opalEye)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opalEye)).isEqualTo(4);
    }

    @Test
    @DisplayName("Activating the tap ability prompts for a damage source choice")
    void tapAbilityPromptsForSourceChoice() {
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
        addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, opalEye), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Noncombat damage the chosen source would deal to a player is dealt to Opal-Eye instead")
    void redirectsNoncombatPlayerDamageToSelf() {
        harness.setLife(player2, 20);
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

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
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
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
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
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
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
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
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

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
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
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
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, opalEye), 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, opalEye.getId());
        harness.passBothPriorities();

        assertThat(opalEye.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("The {1}{W} prevention shield applies only to the next damage")
    void preventionShieldAppliesOnlyToNextDamage() {
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, opalEye), 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, opalEye.getId());
        harness.passBothPriorities();
        assertThat(opalEye.getMarkedDamage()).isZero();

        pyromancer.untap();
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, opalEye.getId());
        harness.passBothPriorities();

        assertThat(opalEye.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The tap ability can choose a damage-dealing spell on the stack as its source")
    void canChooseSpellOnStackAsDamageSource() {
        harness.setLife(player2, 20);
        Permanent opalEye = addCreatureReady(player1, new OpalEyeKondasYojimbo());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        FirstVolley firstVolley = new FirstVolley();
        harness.setHand(player1, List.of(firstVolley));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, victim.getId());
        harness.activateAbility(player1, indexOf(player1, opalEye), 0, null, null);
        harness.passBothPriorities();

        assertThatCode(() -> harness.handlePermanentChosen(player1, firstVolley.getId()))
                .doesNotThrowAnyException();
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(opalEye.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
