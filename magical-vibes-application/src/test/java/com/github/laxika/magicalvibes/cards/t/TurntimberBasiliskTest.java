package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OranRiefRecluse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurntimberBasilisk.class, Forest.class, OranRiefRecluse.class})
class TurntimberBasiliskTest extends BaseCardTest {

    private Permanent triggerLandfall(Player targetController) {
        Permanent target = harness.addToBattlefieldAndReturn(targetController, new OranRiefRecluse());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        return target;
    }

    @Test
    @DisplayName("Landfall can make a target creature block Turntimber Basilisk")
    void landfallSetsMustBlock() {
        Permanent basilisk = harness.addToBattlefieldAndReturn(player1, new TurntimberBasilisk());

        Permanent target = triggerLandfall(player2);

        assertThat(target.getMustBlockIds()).containsExactly(basilisk.getId());
    }

    @Test
    @DisplayName("Declining landfall imposes no block requirement")
    void decliningLandfallImposesNoRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OranRiefRecluse());
        harness.addToBattlefield(player1, new TurntimberBasilisk());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMustBlockIds()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's land does not trigger Turntimber Basilisk")
    void opponentLandDoesNotTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OranRiefRecluse());
        harness.addToBattlefield(player1, new TurntimberBasilisk());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The chosen creature must block Turntimber Basilisk when it attacks")
    void chosenCreatureMustBlockBasilisk() {
        Permanent basilisk = harness.addToBattlefieldAndReturn(player1, new TurntimberBasilisk());
        triggerLandfall(player2);

        basilisk.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("A tapped creature is not required to block")
    void tappedCreatureNeedNotBlock() {
        Permanent basilisk = harness.addToBattlefieldAndReturn(player1, new TurntimberBasilisk());
        Permanent target = triggerLandfall(player2);
        target.tap();

        basilisk.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Landfall can target a creature controlled by its controller")
    void canTargetFriendlyCreature() {
        Permanent basilisk = harness.addToBattlefieldAndReturn(player1, new TurntimberBasilisk());
        Permanent target = triggerLandfall(player1);

        assertThat(target.getMustBlockIds()).containsExactly(basilisk.getId());
    }

    @Test
    @DisplayName("The block requirement expires at the end of the turn")
    void requirementExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new TurntimberBasilisk());
        Permanent target = triggerLandfall(player2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("A removed target makes the landfall ability fizzle")
    void removedTargetMakesAbilityFizzle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OranRiefRecluse());
        harness.addToBattlefield(player1, new TurntimberBasilisk());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("Landfall still resolves after the basilisk leaves the battlefield")
    void abilityResolvesAfterSourceLeaves() {
        Permanent basilisk = harness.addToBattlefieldAndReturn(player1, new TurntimberBasilisk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OranRiefRecluse());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, basilisk));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMustBlockIds()).containsExactly(basilisk.getId());
    }

    @Test
    @DisplayName("Deathtouch destroys a forced blocker with more toughness than the basilisk's power")
    void deathtouchDestroysForcedBlocker() {
        Permanent basilisk = harness.addToBattlefieldAndReturn(player1, new TurntimberBasilisk());
        triggerLandfall(player2);

        basilisk.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Oran-Rief Recluse");
        harness.assertNotOnBattlefield(player2, "Oran-Rief Recluse");
        harness.assertInGraveyard(player1, "Turntimber Basilisk");
    }
}
