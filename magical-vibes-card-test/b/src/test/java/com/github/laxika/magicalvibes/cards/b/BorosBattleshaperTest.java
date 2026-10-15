package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MazeBehemoth;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorosBattleshaper.class, MazeBehemoth.class})
class BorosBattleshaperTest extends BaseCardTest {

    @Test
    @DisplayName("First target controlled by the active player is forced to attack")
    void firstTargetOfActivePlayerMustAttack() {
        Permanent bears = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        chooseTarget(bears.getId());
        decline();
        harness.passBothPriorities();

        assertThat(gqs.hasActiveStaticEffectIncludingGranted(gd, bears, MustAttackEffect.class)).isTrue();

    }

    @Test
    @DisplayName("First target controlled by a defending player is forced to block instead")
    void firstTargetOfDefendingPlayerMustBlock() {
        Permanent bears = addCreatureReady(player2, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        chooseTarget(bears.getId());
        decline();
        harness.passBothPriorities();

        assertThat(gqs.hasActiveStaticEffectIncludingGranted(gd, bears, MustBlockEachCombatEffect.class)).isTrue();

    }

    @Test
    @DisplayName("Second target can't block")
    void secondTargetCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());
        Permanent attacker = addCreatureReady(player1, new MazeBehemoth());

        advanceToCombat(player1);
        decline();
        chooseTarget(blocker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Second target can't attack")
    void secondTargetCannotAttack() {
        Permanent bears = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        decline();
        chooseTarget(bears.getId());
        harness.passBothPriorities();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Each half applies to its own target when both are chosen")
    void bothHalvesApplyToTheirOwnTarget() {
        Permanent forced = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());
        Permanent locked = addCreatureReady(player1, new MazeBehemoth());

        advanceToCombat(player1);
        chooseTarget(forced.getId());
        chooseTarget(locked.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasActiveStaticEffectIncludingGranted(gd, forced, MustAttackEffect.class)).isTrue();
        assertThat(gqs.hasActiveStaticEffectIncludingGranted(gd, locked, MustAttackEffect.class)).isFalse();

        int lockedIndex = gd.playerBattlefields.get(player1.getId()).indexOf(locked);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(lockedIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Declining both halves leaves every creature unaffected")
    void decliningBothHalvesDoesNothing() {
        Permanent bears = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        decline();
        decline();
        harness.passBothPriorities();




        int index = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        assertThatCode(() -> declareAttackers(player1, List.of(index))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Triggers during an opponent's combat too")
    void triggersDuringOpponentCombat() {
        Permanent bears = addCreatureReady(player2, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player2);
        chooseTarget(bears.getId());
        decline();
        harness.passBothPriorities();

        assertThat(gqs.hasActiveStaticEffectIncludingGranted(gd, bears, MustAttackEffect.class)).isTrue();
    }

    @Test
    @DisplayName("The same creature may be chosen for both target clauses")
    void sameCreatureMayBeChosenForBothClauses() {
        Permanent creature = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        chooseTarget(creature.getId());
        assertThatCode(() -> chooseTarget(creature.getId())).doesNotThrowAnyException();
        harness.passBothPriorities();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The prohibition affects only the second target")
    void firstTargetRemainsAbleToAttackWhenBothTargetsAreChosen() {
        Permanent forced = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());
        Permanent locked = addCreatureReady(player1, new MazeBehemoth());

        advanceToCombat(player1);
        chooseTarget(forced.getId());
        chooseTarget(locked.getId());
        harness.passBothPriorities();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(forced);
        assertThatCode(() -> declareAttackers(player1, List.of(index))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The attack requirement expires after the combat")
    void attackRequirementExpiresAfterCombat() {
        Permanent creature = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        chooseTarget(creature.getId());
        decline();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The block requirement expires after the combat")
    void blockRequirementExpiresAfterCombat() {
        Permanent creature = addCreatureReady(player2, new MazeBehemoth());
        Permanent attacker = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        chooseTarget(creature.getId());
        decline();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The attack prohibition expires after the combat")
    void attackProhibitionExpiresAfterCombat() {
        Permanent creature = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        decline();
        chooseTarget(creature.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        assertThatCode(() -> declareAttackers(player1, List.of(index))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A remaining legal second target is still prohibited when the first target leaves")
    void secondTargetStillAffectedWhenFirstTargetLeaves() {
        Permanent forced = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());
        Permanent locked = addCreatureReady(player1, new MazeBehemoth());

        advanceToCombat(player1);
        chooseTarget(forced.getId());
        chooseTarget(locked.getId());
        gd.playerBattlefields.get(player1.getId()).remove(forced);
        gd.playerGraveyards.get(player1.getId()).add(forced.getCard());
        harness.passBothPriorities();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(locked);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An able first target cannot be omitted from attackers")
    void ableFirstTargetMustBeDeclaredAsAttacker() {
        Permanent creature = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        chooseTarget(creature.getId());
        decline();
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped first target is not required to attack")
    void tappedFirstTargetDoesNotHaveToAttack() {
        Permanent creature = addCreatureReady(player1, new MazeBehemoth());
        creature.tap();
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        chooseTarget(creature.getId());
        decline();
        harness.passBothPriorities();

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An able defending first target cannot be omitted from blockers")
    void ableFirstTargetMustBeDeclaredAsBlocker() {
        Permanent creature = addCreatureReady(player2, new MazeBehemoth());
        Permanent attacker = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        chooseTarget(creature.getId());
        decline();
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A remaining legal first target is still required to attack when the second leaves")
    void firstTargetStillAffectedWhenSecondTargetLeaves() {
        Permanent forced = addCreatureReady(player1, new MazeBehemoth());
        addCreatureReady(player1, new BorosBattleshaper());
        Permanent locked = addCreatureReady(player1, new MazeBehemoth());

        advanceToCombat(player1);
        chooseTarget(forced.getId());
        chooseTarget(locked.getId());
        gd.playerBattlefields.get(player1.getId()).remove(locked);
        gd.playerGraveyards.get(player1.getId()).add(locked.getCard());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(forced);
        assertThatCode(() -> declareAttackers(player1, List.of(index))).doesNotThrowAnyException();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    /** Answers the current "up to one target creature" prompt with the given creature. */
    private void chooseTarget(java.util.UUID permanentId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, permanentId);
    }

    /** Declines the current "up to one" prompt by answering with the controller's own player id. */
    private void decline() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
    }
}
