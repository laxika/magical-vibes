package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctumGuardian.class, GrizzlyBears.class, AnabaShaman.class, Shock.class, ChandraNalaar.class})
class SanctumGuardianTest extends BaseCardTest {

    // ===== Activation / source choice =====

    @Test
    @DisplayName("Activating the ability sacrifices the Guardian and puts the ability on the stack")
    void activatingSacrificesAndPutsOnStack() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());

        harness.activateAbility(player1, indexOf(player1, guardian), null, player1.getId());

        harness.assertNotOnBattlefield(player1, "Sanctum Guardian");
        harness.assertInGraveyard(player1, "Sanctum Guardian");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Can choose a damage spell on the stack as the source")
    void preventsDamageFromSpellOnStack() {
        harness.setLife(player1, 20);
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Shock shock = new Shock();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(shock.getId());
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Choosing a source records a one-shot any-target prevention shield")
    void choosingSourceRecordsShield() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent source = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .extracting(s -> s.sourceId())
                .containsExactly(source.getId());
    }

    // ===== Noncombat damage =====

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen source to a creature and is consumed")
    void preventsNoncombatDamageToCreature() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());
        Permanent creature = addReadyStats(player2, 3, 3);

        harness.activateAbility(player1, indexOf(player1, guardian), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shaman.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen source to a player")
    void preventsNoncombatDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());

        harness.activateAbility(player1, indexOf(player1, guardian), null, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shaman.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a source other than the chosen one is not prevented")
    void doesNotAffectNonChosenSource() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());
        Permanent decoy = addReadyStats(player1, 2, 2);
        Permanent creature = addReadyStats(player2, 3, 3);

        harness.activateAbility(player1, indexOf(player1, guardian), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, decoy.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .extracting(s -> s.sourceId())
                .containsExactly(decoy.getId());
    }

    // ===== Combat damage =====

    @Test
    @DisplayName("Prevents combat damage from the chosen attacker to the defending player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent attacker = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents combat damage from the chosen attacker to a blocking creature")
    void preventsCombatDamageToCreature() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent blocker = addReadyStats(player1, 3, 3);
        Permanent attacker = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, blocker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, blocker), 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    // ===== Cleanup / no valid sources =====

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent source = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("The sacrificed Guardian remains a legal source referenced by its resolving ability")
    void canChooseSacrificedGuardian() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());

        harness.activateAbility(player1, indexOf(player1, guardian), null, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(guardian.getId());
        harness.handlePermanentChosen(player1, guardian.getId());
    }

    @Test
    @DisplayName("Prevents the chosen source's next damage to a planeswalker")
    void preventsDamageToPlaneswalker() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent source = addCreatureReady(player2, new AnabaShaman());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, indexOf(player1, guardian), null, planeswalker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, source), null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Answering the source choice resumes the parked resolution entry")
    void answeringSourceChoiceClearsParkedResolution() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent source = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();

        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    @Test
    @DisplayName("Prevents simultaneous damage only to the targeted player, not their creatures")
    void onlyProtectsTargetDuringSimultaneousDamage() {
        harness.setLife(player2, 20);
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 9);
        Permanent firstCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, guardian), null, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chandra.getId());

        harness.activateAbility(player1, indexOf(player1, chandra), 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstCreature, secondCreature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(firstCreature.getCard(), secondCreature.getCard());
    }

    @Test
    @DisplayName("Damage from a later activation of the chosen source is not prevented")
    void onlyPreventsFirstDamageEvent() {
        harness.setLife(player2, 20);
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());

        harness.activateAbility(player1, indexOf(player1, guardian), null, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shaman.getId());

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, indexOf(player1, shaman), null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 20);

        shaman.setTapped(false);
        harness.activateAbility(player1, indexOf(player1, shaman), null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A sacrificed creature targeted by a spell on the stack remains a legal source choice")
    void canChooseDepartedSpellTarget() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, guardian.getId());

        harness.activateAbility(player1, indexOf(player1, guardian), null, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(guardian.getId(), shock.getId());
        harness.handlePermanentChosen(player1, guardian.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Activation requires a target before the Guardian is sacrificed")
    void cannotActivateWithoutTarget() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, guardian), null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sanctum Guardian");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage to another player neither receives prevention nor consumes the target's shield")
    void damageToOtherRecipientDoesNotConsumeShield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());

        harness.activateAbility(player1, indexOf(player1, guardian), null, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shaman.getId());

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, indexOf(player1, shaman), null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);

        shaman.setTapped(false);
        harness.activateAbility(player1, indexOf(player1, shaman), null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An ability targeting the sacrificed Guardian does not resolve or ask for a source")
    void doesNotResolveWhenTargetLeavesBattlefield() {
        Permanent guardian = addCreatureReady(player1, new SanctumGuardian());
        addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, guardian), null, guardian.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
        assertThat(gd.stack).isEmpty();
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
