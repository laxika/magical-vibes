package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrisonTermTest extends BaseCardTest {

    // ===== Enchant + lockdown =====

    @Test
    @DisplayName("Resolving Prison Term attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = readyCreature(player2);

        harness.setHand(player1, List.of(new PrisonTerm()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Prison Term")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent bears = readyCreature(player1);
        attachedPrisonTerm(player1, bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent gnomes = new Permanent(new com.github.laxika.magicalvibes.cards.b.BottleGnomes());
        gnomes.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gnomes);
        attachedPrisonTerm(player2, gnomes);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    // ===== Jump-to-entering-opponent-creature trigger =====

    @Test
    @DisplayName("Accepting the may moves Prison Term onto the opponent creature that entered")
    void movesToEnteringOpponentCreatureOnAccept() {
        Permanent original = readyCreature(player1);
        Permanent prison = attachedPrisonTerm(player1, original);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities(); // resolve creature → Prison Term triggers, may-ability on stack
        harness.passBothPriorities(); // resolve may-ability → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = bearsOnBattlefield(player2);
        assertThat(prison.getAttachedTo()).isEqualTo(entered.getId());
    }

    @Test
    @DisplayName("Declining the may leaves Prison Term on the original creature")
    void staysOnOriginalOnDecline() {
        Permanent original = readyCreature(player1);
        Permanent prison = attachedPrisonTerm(player1, original);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
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
        Permanent original = readyCreature(player1);
        attachedPrisonTerm(player1, original);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature — no trigger for own creature

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // a legal creature target exists
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.FountainOfYouth());
        harness.setHand(player1, List.of(new PrisonTerm()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helpers =====

    private Permanent readyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent attachedPrisonTerm(Player controller, Permanent creature) {
        Permanent prison = new Permanent(new PrisonTerm());
        prison.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(prison);
        return prison;
    }

    private Permanent bearsOnBattlefield(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
    }
}
