package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.d.DroveOfElves;
import com.github.laxika.magicalvibes.cards.e.ElsewhereFlask;
import com.github.laxika.magicalvibes.cards.m.MistmeadowSkulk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrisonTerm.class, BallynockCohort.class, DevotedDruid.class, ElsewhereFlask.class, MistmeadowSkulk.class, DroveOfElves.class})
class PrisonTermTest extends BaseCardTest {

    // ===== Enchant + lockdown =====

    @Test
    @DisplayName("Resolving Prison Term attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player2, new BallynockCohort());

        harness.setHand(player1, List.of(new PrisonTerm()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Prison Term")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new BallynockCohort());
        attachedPrisonTerm(player1, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new BallynockCohort());
        attacker.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new BallynockCohort());
        attachedPrisonTerm(player1, creature);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());
        attachedPrisonTerm(player2, druid);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    // ===== Jump-to-entering-opponent-creature trigger =====

    @Test
    @DisplayName("Accepting the may moves Prison Term onto the opponent creature that entered")
    void movesToEnteringOpponentCreatureOnAccept() {
        Permanent original = addCreatureReady(player1, new BallynockCohort());
        Permanent prison = attachedPrisonTerm(player1, original);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BallynockCohort()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities(); // resolve creature → Prison Term triggers, may-ability on stack
        harness.passBothPriorities(); // resolve may-ability → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = findPermanent(player2, "Ballynock Cohort");
        assertThat(prison.getAttachedTo()).isEqualTo(entered.getId());
    }

    @Test
    @DisplayName("Declining the may leaves Prison Term on the original creature")
    void staysOnOriginalOnDecline() {
        Permanent original = addCreatureReady(player1, new BallynockCohort());
        Permanent prison = attachedPrisonTerm(player1, original);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BallynockCohort()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(prison.getAttachedTo()).isEqualTo(original.getId());
    }

    @Test
    @DisplayName("Does not trigger for a creature the controller controls entering")
    void doesNotTriggerForOwnCreature() {
        Permanent original = addCreatureReady(player1, new BallynockCohort());
        attachedPrisonTerm(player1, original);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BallynockCohort()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature — no trigger for own creature

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new BallynockCohort()); // a legal creature target exists
        harness.addToBattlefield(player1, new ElsewhereFlask());
        harness.setHand(player1, List.of(new PrisonTerm()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Elsewhere Flask");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Protection prevents moving Prison Term and leaves its original attachment intact")
    void cannotMoveToCreatureWithProtection() {
        Permanent original = addCreatureReady(player1, new BallynockCohort());
        Permanent prison = attachedPrisonTerm(player1, original);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MistmeadowSkulk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(prison);
        assertThat(prison.getAttachedTo()).isEqualTo(original.getId());
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its untap ability")
    void enchantedCreatureCannotActivateNonManaAbility() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());
        druid.setTapped(true);
        attachedPrisonTerm(player2, druid);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(druid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prison Term can move onto an entering opponent creature with hexproof")
    void movesToEnteringCreatureWithHexproof() {
        Permanent original = addCreatureReady(player1, new BallynockCohort());
        Permanent prison = attachedPrisonTerm(player1, original);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DroveOfElves()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(prison.getAttachedTo()).isEqualTo(findPermanent(player2, "Drove of Elves").getId());
    }
    private Permanent attachedPrisonTerm(Player controller, Permanent creature) {
        Permanent prison = harness.addToBattlefieldAndReturn(controller, new PrisonTerm());
        prison.setAttachedTo(creature.getId());
        return prison;
    }
}
