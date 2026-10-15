package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.v.VulshokReplica;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuriokReplica.class, GoldMyr.class, GalvanicBlast.class, VulshokReplica.class})
class AuriokReplicaTest extends BaseCardTest {

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
        harness.assertOnBattlefield(player1, "Auriok Replica");
    }

    @Test
    @DisplayName("Activating ability sacrifices Auriok Replica and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addReadyReplica(player1);
        addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        // Auriok Replica should be sacrificed (not on battlefield, in graveyard)
        harness.assertNotOnBattlefield(player1, "Auriok Replica");
        harness.assertInGraveyard(player1, "Auriok Replica");

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
        addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // After ability resolves, player should be prompted to choose a permanent
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();
    }

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

        opponentCreature.setAttacking(true);
        resolveCombat(player2);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Non-chosen source's damage to controller is NOT prevented")
    void doesNotPreventDamageFromNonChosenSource() {
        harness.setLife(player1, 20);
        addReadyReplica(player1);
        Permanent creature1 = addReadyCreature(player2, "Auriok Replica");
        Permanent creature2 = addReadyCreature(player2, "Gold Myr");
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate ability, resolve, choose creature1 as the source
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature1.getId());

        // Creature1 is prevented, creature2 is NOT
        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId()))
                .contains(creature1.getId())
                .doesNotContain(creature2.getId());

        creature1.setAttacking(true);
        creature2.setAttacking(true);
        resolveCombat(player2);
        harness.assertLife(player1, 19);
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId()))
                .contains(opponentCreature.getId());
        harness.passUntil(player2, TurnStep.UPKEEP);

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
        harness.assertNotOnBattlefield(player1, "Auriok Replica");
        harness.assertInGraveyard(player1, "Auriok Replica");
    }

    @Test
    @DisplayName("Can choose own permanent as source to prevent")
    void canChooseOwnPermanentAsSource() {
        addReadyReplica(player1);
        Permanent ownCreature = addReadyCreature(player1, "Auriok Replica");
        addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose own creature
        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId()))
                .contains(ownCreature.getId());
    }

    @Test
    @DisplayName("Answering the source choice resumes the parked resolution entry")
    void answeringSourceChoiceClearsParkedResolution() {
        addReadyReplica(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();

        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    @Test
    @DisplayName("A spell on the stack can be chosen and its damage is prevented")
    void preventsDamageFromSpellOnStack() {
        harness.addToBattlefield(player1, new AuriokReplica());
        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        java.util.UUID sourceId = gd.stack.getLast().getTargetableId();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sourceId);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A sacrificed source referenced by an ability on the stack can be chosen")
    void canChooseSacrificedSourceOfPendingAbility() {
        harness.addToBattlefield(player1, new AuriokReplica());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new VulshokReplica());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.assertInGraveyard(player2, "Vulshok Replica");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(source.getId());
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevention remains effective during the end step")
    void preventsDamageDuringEndStep() {
        harness.addToBattlefield(player1, new AuriokReplica());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new VulshokReplica());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Choosing a source does not prevent its damage to the opponent")
    void doesNotPreventDamageToOtherPlayer() {
        harness.addToBattlefield(player1, new AuriokReplica());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new VulshokReplica());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The chosen source is prevented in multiple damage events in the same turn")
    void preventsMultipleDamageEvents() {
        harness.addToBattlefield(player1, new AuriokReplica());
        Permanent attacker = addCreatureReady(player2, new AuriokReplica());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        attacker.setAttacking(true);
        harness.resolveCombatDamage();
        harness.assertLife(player1, 20);
        attacker.setAttacking(true);
        harness.resolveCombatDamage();
        harness.assertLife(player1, 20);
    }

    private Permanent addReadyReplica(Player player) {
        return addCreatureReady(player, new AuriokReplica());
    }

    private Permanent addReadyCreature(Player player) {
        return addReadyCreature(player, null);
    }

    private Permanent addReadyCreature(Player player, String type) {
        return addCreatureReady(player, "Gold Myr".equals(type)
                ? new GoldMyr() : new AuriokReplica());
    }
}
