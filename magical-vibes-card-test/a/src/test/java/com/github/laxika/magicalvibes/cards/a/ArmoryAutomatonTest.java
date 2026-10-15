package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoryAutomaton.class, AccordersShield.class})
class ArmoryAutomatonTest extends BaseCardTest {

    @Test
    void entersAndAttachesAnyTargetedEquipmentWithoutChangingControl() {
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        harness.setHand(player1, List.of(new ArmoryAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, ownEquipment.getId());
        harness.handlePermanentChosen(player1, opposingEquipment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent automaton = findPermanent(player1, "Armory Automaton");
        assertThat(ownEquipment.getAttachedTo()).isEqualTo(automaton.getId());
        assertThat(opposingEquipment.getAttachedTo()).isEqualTo(automaton.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingEquipment);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownEquipment);
    }

    @Test
    void attackAttachesAnyTargetedEquipment() {
        Permanent automaton = addCreatureReady(player1, new ArmoryAutomaton());
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(automaton)));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, ownEquipment.getId());
        harness.handlePermanentChosen(player1, opposingEquipment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownEquipment.getAttachedTo()).isEqualTo(automaton.getId());
        assertThat(opposingEquipment.getAttachedTo()).isEqualTo(automaton.getId());
    }

    @Test
    void canDeclineAttachmentAtResolutionAfterChoosingEtbTargets() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        harness.setHand(player1, List.of(new ArmoryAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDeclineAttachmentAtResolutionAfterChoosingAttackTargets() {
        Permanent automaton = addCreatureReady(player1, new ArmoryAutomaton());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(automaton)));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canChooseZeroEquipmentOnAttack() {
        Permanent automaton = addCreatureReady(player1, new ArmoryAutomaton());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(automaton)));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void equipmentStaysOnItsPreviousCreatureWhenAutomatonLeavesBeforeResolution() {
        Permanent previousHost = harness.addToBattlefieldAndReturn(player2, new ArmoryAutomaton());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        equipment.setAttachedTo(previousHost.getId());
        Permanent automaton = addCreatureReady(player1, new ArmoryAutomaton());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(automaton)));
        harness.handlePermanentChosen(player1, equipment.getId());
        gd.playerBattlefields.get(player1.getId()).remove(automaton);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(equipment.getAttachedTo()).isEqualTo(previousHost.getId());
    }

    @Test
    void canChooseZeroEquipmentOnEntering() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        harness.setHand(player1, List.of(new ArmoryAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void remainingLegalEquipmentMovesFromItsPreviousCreatureWhenAnotherTargetLeaves() {
        Permanent previousHost = harness.addToBattlefieldAndReturn(player2, new ArmoryAutomaton());
        Permanent survivingEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        survivingEquipment.setAttachedTo(previousHost.getId());
        Permanent removedEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        Permanent automaton = addCreatureReady(player1, new ArmoryAutomaton());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(automaton)));
        harness.handlePermanentChosen(player1, survivingEquipment.getId());
        harness.handlePermanentChosen(player1, removedEquipment.getId());
        gd.playerBattlefields.get(player2.getId()).remove(removedEquipment);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(survivingEquipment.getAttachedTo()).isEqualTo(automaton.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivingEquipment);
    }

    @Test
    void canTargetMoreThanNinetyNineEquipment() {
        Permanent automaton = addCreatureReady(player1, new ArmoryAutomaton());
        List<Permanent> equipment = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new AccordersShield()))
                .toList();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(automaton)));
        for (int i = 0; i < 99; i++) {
            harness.handlePermanentChosen(player1, equipment.get(i).getId());
        }

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, equipment.get(99).getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.withAutoStop(gd.currentStep, () -> {
                harness.clearPriorityPassed();
                harness.handleMayAbilityChosen(player1, true);
            });
        }

        assertThat(equipment).allSatisfy(item -> assertThat(item.getAttachedTo()).isEqualTo(automaton.getId()));
    }
}
