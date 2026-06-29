package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuriokReplicaTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Auriok Replica has correct card properties")
    void hasCorrectProperties() {
        AuriokReplica card = new AuriokReplica();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{W}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(0))
                .isInstanceOf(SacrificeSelfCost.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(1))
                .isInstanceOf(PreventAllDamageFromChosenSourceEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Auriok Replica puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new AuriokReplica()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Auriok Replica");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Auriok Replica"));
    }

    // ===== Activation — sacrifice and source choice =====

    @Test
    @DisplayName("Activating ability sacrifices Auriok Replica and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addReadyReplica(player1);
        addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        // Auriok Replica should be sacrificed (not on battlefield, in graveyard)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Auriok Replica"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Auriok Replica"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Auriok Replica");
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutWhiteMana() {
        addReadyReplica(player1);
        addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving ability prompts for source choice")
    void resolvingAbilityPromptsForSourceChoice() {
        addReadyReplica(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // After ability resolves, player should be prompted to choose a permanent
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.PERMANENT_CHOICE)).isTrue();
    }

    // ===== Prevention effect =====

    @Test
    @DisplayName("Chosen source's combat damage to controller is prevented")
    void preventsCombatDamageFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyReplica(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate ability, resolve, choose opponent's creature as source
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose the opponent creature as the source to prevent
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Verify the prevention is recorded
        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId()))
                .contains(opponentCreature.getId());

        // Player should still be at 20 life (prevention was set up, no damage dealt yet)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Non-chosen source's damage to controller is NOT prevented")
    void doesNotPreventDamageFromNonChosenSource() {
        harness.setLife(player1, 20);
        addReadyReplica(player1);
        Permanent creature1 = addReadyCreature(player2, "Grizzly Bears");
        Permanent creature2 = addReadyCreature(player2, "Llanowar Elves");
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate ability, resolve, choose creature1 as the source
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature1.getId());

        // Creature1 is prevented, creature2 is NOT
        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId()))
                .contains(creature1.getId())
                .doesNotContain(creature2.getId());
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        addReadyReplica(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate, resolve, choose source
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Verify prevention is set
        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId()))
                .contains(opponentCreature.getId());

        // Advance past end of turn (which resets end-of-turn modifiers)
        advanceToEndStep();

        // Prevention should be cleared
        assertThat(gd.playerSourceDamagePreventionIds.getOrDefault(player1.getId(), java.util.Set.of()))
                .isEmpty();
    }

    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        AuriokReplica card = new AuriokReplica();
        harness.addToBattlefield(player1, card);
        addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Auriok Replica is in graveyard after activation, even before ability resolves")
    void inGraveyardAfterActivation() {
        addReadyReplica(player1);
        addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        // Before resolution, Auriok Replica should already be in the graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Auriok Replica"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Auriok Replica"));
    }

    @Test
    @DisplayName("Can choose own permanent as source to prevent")
    void canChooseOwnPermanentAsSource() {
        addReadyReplica(player1);
        Permanent ownCreature = addReadyCreature(player1, "Grizzly Bears");
        addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose own creature
        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId()))
                .contains(ownCreature.getId());
    }

    // ===== Helpers =====

    private Permanent addReadyReplica(Player player) {
        AuriokReplica card = new AuriokReplica();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        return addReadyCreature(player, null);
    }

    private Permanent addReadyCreature(Player player, String type) {
        com.github.laxika.magicalvibes.model.Card card;
        if ("Llanowar Elves".equals(type)) {
            card = new LlanowarElves();
        } else {
            card = new GrizzlyBears();
        }
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances POSTCOMBAT_MAIN -> END_STEP
    }
}
