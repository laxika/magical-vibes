package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JadeMonolithTest extends BaseCardTest {

    // ===== Activation / source choice =====

    @Test
    @DisplayName("Activating the ability targeting a creature prompts for a source choice")
    void activatingPromptsForSourceChoice() {
        Permanent monolith = addReadyPermanent(player1, new JadeMonolith());
        Permanent creature = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, monolith), null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();
    }

    @Test
    @DisplayName("Choosing a source registers a next-event redirect shield pointing at the controller")
    void choosingSourceCreatesNextEventShield() {
        Permanent monolith = addReadyPermanent(player1, new JadeMonolith());
        Permanent creature = addReadyStats(player2, 3, 3);
        Permanent source = addReadyStats(player2, 2, 2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, monolith), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);
        var shield = gd.creatureDamageRedirectShields.getFirst();
        assertThat(shield.protectedPermanentId()).isEqualTo(creature.getId());
        assertThat(shield.damageSourceId()).isEqualTo(source.getId());
        assertThat(shield.remainingAmount()).isEqualTo(CreatureDamageRedirectShield.NEXT_EVENT);
        assertThat(shield.redirectTargetId()).isEqualTo(player1.getId());
    }

    // ===== Noncombat damage redirect =====

    @Test
    @DisplayName("Noncombat damage from the chosen source to the target creature is dealt to the controller instead")
    void redirectsNoncombatDamageToController() {
        Permanent monolith = addReadyPermanent(player1, new JadeMonolith());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent protectedCreature = addReadyStats(player2, 3, 3);

        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, monolith), null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        // Prodigal Pyromancer pings the protected creature — the damage is redirected to Jade Monolith's controller
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        // "The next time" — one event only; the shield is consumed after redirecting.
        assertThat(gd.creatureDamageRedirectShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a source other than the chosen one is not redirected")
    void doesNotAffectNonMatchingSource() {
        Permanent monolith = addReadyPermanent(player1, new JadeMonolith());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent decoySource = addReadyStats(player1, 2, 2);
        Permanent protectedCreature = addReadyStats(player2, 3, 3);

        int lifeBefore = gd.getLife(player1.getId());

        // Choose the decoy (not the pyromancer) as the redirected source
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, monolith), null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, decoySource.getId());

        // Pyromancer (not the chosen source) pings the protected creature — damage lands normally
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Combat damage redirect =====

    @Test
    @DisplayName("Combat damage from the chosen attacker to the target creature is dealt to the controller instead")
    void redirectsCombatDamageToController() {
        Permanent monolith = addReadyPermanent(player1, new JadeMonolith());
        Permanent blocker = addReadyStats(player1, 3, 3);
        Permanent attacker = addReadyStats(player2, 2, 2);

        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, monolith), null, blocker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        // player2 attacks with the chosen source, player1's blocker blocks it
        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, blocker), 0)));
        harness.passBothPriorities();

        // Blocker takes no damage; the 2 combat damage is redirected to Jade Monolith's controller
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    // ===== Cleanup =====

    @Test
    @DisplayName("Creature damage redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent monolith = addReadyPermanent(player1, new JadeMonolith());
        Permanent creature = addReadyStats(player2, 3, 3);
        Permanent source = addReadyStats(player2, 2, 2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, monolith), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
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
