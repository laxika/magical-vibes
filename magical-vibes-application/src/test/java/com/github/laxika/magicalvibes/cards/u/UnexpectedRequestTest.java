package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DwarvenCastleGuard;
import com.github.laxika.magicalvibes.cards.d.DragoonsLance;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnexpectedRequest.class, DwarvenCastleGuard.class, DragoonsLance.class})
class UnexpectedRequestTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps, and grants haste before offering the Equipment choice")
    void stealsUntapsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        target.tap();
        harness.addToBattlefield(player1, new DragoonsLance());

        castAndResolveUnexpectedRequest(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Attaches a chosen Equipment and unattaches it at the next end step")
    void attachesAndUnattachesAtNextEndStep() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DragoonsLance());

        castAndResolveUnexpectedRequest(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Chooses one of multiple controlled Equipment")
    void choosesOneOfMultipleEquipment() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new DragoonsLance());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new DragoonsLance());

        castAndResolveUnexpectedRequest(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondEquipment.getId());

        assertThat(secondEquipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(firstEquipment.getAttachedTo()).isNull();
    }

    @Test
    void decliningAttachmentLeavesEquipmentWhereItWas() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        Permanent originalHost = addCreatureReady(player1, new DwarvenCastleGuard());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DragoonsLance());
        equipment.setAttachedTo(originalHost.getId());

        castAndResolveUnexpectedRequest(target);
        harness.handleMayAbilityChosen(player1, false);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(equipment.getAttachedTo()).isEqualTo(originalHost.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentEquipmentCannotBeChosen() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        Permanent controlledEquipment = harness.addToBattlefieldAndReturn(player1, new DragoonsLance());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new DragoonsLance());

        castAndResolveUnexpectedRequest(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlledEquipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(opponentEquipment.getAttachedTo()).isNull();
    }

    @Test
    void canTargetOwnCreatureWithoutEquipment() {
        Permanent target = addCreatureReady(player1, new DwarvenCastleGuard());
        target.tap();

        castAndResolveUnexpectedRequest(target);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void delayedAbilityUnattachesEquipmentEvenAfterItMovesToAnotherCreature() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DragoonsLance());
        Permanent otherCreature = addCreatureReady(player1, new DwarvenCastleGuard());

        castAndResolveUnexpectedRequest(target);
        harness.handleMayAbilityChosen(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(otherCreature.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void delayedAbilityStillTriggersWhenEquipmentHasBecomeUnattached() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DragoonsLance());

        castAndResolveUnexpectedRequest(target);
        harness.handleMayAbilityChosen(player1, true);
        target.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, harness::passBothPriorities);

        assertThat(equipment.getAttachedTo()).isNull();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
    }

    private void castAndResolveUnexpectedRequest(Permanent target) {
        harness.setHand(player1, List.of(new UnexpectedRequest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
