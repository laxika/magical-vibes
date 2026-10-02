package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcquisitionOctopus.class, ArmguardFamiliar.class})
class AcquisitionOctopusTest extends BaseCardTest {

    @Test
    void unconfiguredOctopusDrawsWhenItDealsCombatDamage() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        octopus.setAttacking(true);
        ArmguardFamiliar drawnCard = new ArmguardFamiliar();
        harness.setLibrary(player1, List.of(drawnCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void equippedCreatureDrawsWhenItDealsCombatDamage() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent creature = addCreatureReady(player1, new ArmguardFamiliar());
        octopus.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        ArmguardFamiliar drawnCard = new ArmguardFamiliar();
        harness.setLibrary(player1, List.of(drawnCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void reconfigureAttachesAndUnattachesTheOctopus() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent creature = addCreatureReady(player1, new ArmguardFamiliar());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(octopus.getAttachedTo()).isEqualTo(creature.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(octopus.getAttachedTo()).isNull();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent opponentCreature = addCreatureReady(player2, new ArmguardFamiliar());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(octopus.getAttachedTo()).isNull();
    }

    @Test
    void attachedOctopusStopsBeingACreatureAndBecomesOneAgainWhenUnattached() {
        Permanent octopus = harness.addToBattlefieldAndReturn(player1, new AcquisitionOctopus());
        Permanent creature = addCreatureReady(player1, new ArmguardFamiliar());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThat(gqs.isCreature(gd, octopus)).isTrue();
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, octopus)).isFalse();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, octopus)).isTrue();
        assertThat(octopus.isSummoningSick()).isTrue();
    }

    @Test
    void reconfigureCanMoveDirectlyBetweenCreatures() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent firstCreature = addCreatureReady(player1, new ArmguardFamiliar());
        Permanent secondCreature = addCreatureReady(player1, new ArmguardFamiliar());
        octopus.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(octopus.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.isCreature(gd, octopus)).isFalse();
    }

    @Test
    void neitherReconfigureModeCanBeActivatedDuringCombat() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent creature = addCreatureReady(player1, new ArmguardFamiliar());
        octopus.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(octopus.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reconfigureDoesNotAttachWhenTargetLeavesBeforeResolution() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent creature = addCreatureReady(player1, new ArmguardFamiliar());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(octopus.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, octopus)).isTrue();
    }

    @Test
    void octopusControllerDrawsWhenAnOpponentsEquippedCreatureDealsCombatDamage() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent creature = addCreatureReady(player2, new ArmguardFamiliar());
        octopus.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        ArmguardFamiliar drawnCard = new ArmguardFamiliar();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void unrelatedAttackingCreatureDoesNotTriggerAttachedOctopus() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent equippedCreature = addCreatureReady(player1, new ArmguardFamiliar());
        Permanent attacker = addCreatureReady(player1, new ArmguardFamiliar());
        octopus.setAttachedTo(equippedCreature.getId());
        attacker.setAttacking(true);
        ArmguardFamiliar libraryCard = new ArmguardFamiliar();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void combatDamageToACreatureDoesNotDrawACard() {
        addCreatureReady(player1, new AcquisitionOctopus());
        addCreatureReady(player2, new ArmguardFamiliar());
        ArmguardFamiliar libraryCard = new ArmguardFamiliar();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Acquisition Octopus");
        harness.assertInGraveyard(player2, "Armguard Familiar");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void octopusBecomesACreatureWhenItsEquippedCreatureDies() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent creature = addCreatureReady(player1, new ArmguardFamiliar());
        octopus.setAttachedTo(creature.getId());
        creature.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Armguard Familiar");
        harness.assertOnBattlefield(player1, "Acquisition Octopus");
        assertThat(octopus.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, octopus)).isTrue();
    }
}
