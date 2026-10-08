package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CombustionMan.class, GarrukWildspeaker.class, GrizzlyBears.class, Mountain.class})
class CombustionManTest extends BaseCardTest {

    @Test
    void targetControllerAcceptsDamageAndTargetSurvives() {
        Permanent combustionMan = addReadyCombustionMan();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2);

        chooseAttackTriggerTarget(combustionMan, target, planeswalker);
        TestCards.mutableCard(combustionMan).setPower(6);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertLife(player2, 14);
    }

    @Test
    void targetControllerDeclinesDamageAndTargetIsDestroyed() {
        Permanent combustionMan = addReadyCombustionMan();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2);

        chooseAttackTriggerTarget(combustionMan, target, planeswalker);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void canDestroyNoncreaturePermanent() {
        Permanent combustionMan = addReadyCombustionMan();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());

        queueAttackTrigger(combustionMan, target);
        resolveDamageChoice(player2, false);

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertLife(player2, 20);
    }

    @Test
    void ownPermanentControllerMakesChoiceAndTakesDamage() {
        Permanent combustionMan = addReadyCombustionMan();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Mountain());

        queueAttackTrigger(combustionMan, target);
        resolveDamageChoice(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    void acceptingPreventedDamageStillSavesPermanent() {
        Permanent combustionMan = addReadyCombustionMan();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        gd.preventAllDamageByCreatures = true;

        queueAttackTrigger(combustionMan, target);
        resolveDamageChoice(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertLife(player2, 20);
    }

    @Test
    void usesPowerAtResolutionInsteadOfPowerAtAttack() {
        Permanent combustionMan = addReadyCombustionMan();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());

        queueAttackTrigger(combustionMan, target);
        combustionMan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveDamageChoice(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertLife(player2, 13);
    }

    @Test
    void zeroPowerAllowsControllerToSavePermanentWithoutDamage() {
        Permanent combustionMan = addReadyCombustionMan();
        combustionMan.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());

        queueAttackTrigger(combustionMan, target);
        resolveDamageChoice(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertLife(player2, 20);
    }

    @Test
    void sourceLeavingBattlefieldDoesNotRemoveDamageChoice() {
        Permanent combustionMan = addReadyCombustionMan();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());

        queueAttackTrigger(combustionMan, target);
        combustionMan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, combustionMan));
        resolveDamageChoice(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertInGraveyard(player1, "Combustion Man");
        harness.assertLife(player2, 14);
    }

    @Test
    void removedTargetDoesNotOfferDamageChoice() {
        Permanent combustionMan = addReadyCombustionMan();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());

        queueAttackTrigger(combustionMan, target);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> harness.passBothPriorities());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertLife(player2, 20);
    }

    private void queueAttackTrigger(Permanent combustionMan, Permanent target) {
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(combustionMan);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(attackerIndex)));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
    }

    private void resolveDamageChoice(Player controller, boolean accepted) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(controller, accepted);
        });
    }

    private Permanent addReadyCombustionMan() {
        return addCreatureReady(player1, new CombustionMan());
    }

    private void chooseAttackTriggerTarget(Permanent combustionMan, Permanent target, Permanent attackTarget) {
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(combustionMan);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(attackerIndex), Map.of(attackerIndex, attackTarget.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new GarrukWildspeaker();
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setCounterCount(CounterType.LOYALTY, 5);
        return permanent;
    }
}
