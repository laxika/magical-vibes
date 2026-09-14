package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AncientHydra;
import com.github.laxika.magicalvibes.cards.f.FlintGolem;
import com.github.laxika.magicalvibes.cards.r.Rupture;
import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({OraclesAttendants.class, AncientHydra.class, FlintGolem.class, Rupture.class, SpinelessThug.class})
class OraclesAttendantsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability targeting a creature prompts for a source choice")
    void activatingPromptsForSourceChoice() {
        Permanent attendants = addCreatureReady(player1, new OraclesAttendants());
        Permanent creature = addCreatureReady(player2, new FlintGolem());

        harness.activateAbility(player1, indexOf(player1, attendants), null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();
    }

    @Test
    @DisplayName("Choosing a source creates a creature damage redirect shield")
    void choosingSourceCreatesShield() {
        Permanent attendants = addCreatureReady(player1, new OraclesAttendants());
        Permanent creature = addCreatureReady(player2, new FlintGolem());
        Permanent source = addCreatureReady(player2, new SpinelessThug());

        harness.activateAbility(player1, indexOf(player1, attendants), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);
        assertThat(gd.creatureDamageRedirectShields.getFirst().protectedPermanentId()).isEqualTo(creature.getId());
        assertThat(gd.creatureDamageRedirectShields.getFirst().damageSourceId()).isEqualTo(source.getId());
        assertThat(gd.creatureDamageRedirectShields.getFirst().redirectTargetId()).isEqualTo(attendants.getId());
    }

    @Test
    @DisplayName("Noncombat damage from the chosen source is dealt to Oracle's Attendants instead")
    void redirectsNoncombatDamageToSelf() {
        Permanent attendants = addCreatureReady(player1, new OraclesAttendants());
        Permanent hydra = addReadyHydra(player1);
        Permanent protectedCreature = addCreatureReady(player2, new FlintGolem());

        harness.activateAbility(player1, indexOf(player1, attendants), null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, hydra.getId());

        harness.activateAbility(player1, indexOf(player1, hydra), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(0);
        assertThat(attendants.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell on the stack is offered as a source choice")
    void offersSpellOnStackAsSource() {
        Permanent attendants = addCreatureReady(player1, new OraclesAttendants());
        Permanent protectedCreature = addCreatureReady(player2, new FlintGolem());
        Rupture rupture = new Rupture();

        harness.setHand(player1, List.of(rupture));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);

        harness.activateAbility(player1, indexOf(player1, attendants), null, protectedCreature.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(rupture.getId());
    }

    @Test
    @DisplayName("Damage from a source other than the chosen one is not redirected")
    void doesNotAffectNonMatchingSource() {
        Permanent attendants = addCreatureReady(player1, new OraclesAttendants());
        Permanent hydra = addReadyHydra(player1);
        Permanent decoySource = addCreatureReady(player1, new SpinelessThug());
        Permanent protectedCreature = addCreatureReady(player2, new FlintGolem());

        harness.activateAbility(player1, indexOf(player1, attendants), null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, decoySource.getId());

        harness.activateAbility(player1, indexOf(player1, hydra), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(attendants.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Combat damage from the chosen attacker to the target creature is dealt to Oracle's Attendants instead")
    void redirectsCombatDamageToSelf() {
        Permanent attendants = addCreatureReady(player1, new OraclesAttendants());
        Permanent blocker = addCreatureReady(player1, new FlintGolem());
        Permanent attacker = addCreatureReady(player2, new SpinelessThug());

        harness.activateAbility(player1, indexOf(player1, attendants), null, blocker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, blocker), 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(attendants.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature damage redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent attendants = addCreatureReady(player1, new OraclesAttendants());
        Permanent creature = addCreatureReady(player2, new FlintGolem());
        Permanent source = addCreatureReady(player2, new SpinelessThug());

        harness.activateAbility(player1, indexOf(player1, attendants), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).isEmpty();
    }

    private Permanent addReadyHydra(Player player) {
        AncientHydra hydra = new AncientHydra();
        Permanent permanent = addCreatureReady(player, hydra);
        permanent.setCounterCount(CounterType.FADE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        return permanent;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
