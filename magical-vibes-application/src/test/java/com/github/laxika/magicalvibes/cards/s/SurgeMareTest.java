package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SurgeMareTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives +2/-2 until end of turn")
    void abilityBoosts() {
        Permanent mare = addMareReady();
        addBlueMana(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mare.getEffectivePower()).isEqualTo(2);
        assertThat(mare.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent mare = addMareReady();
        addBlueMana(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mare.getEffectivePower()).isEqualTo(0);
        assertThat(mare.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Repeated activations stack and can kill Surge Mare via state-based actions")
    void repeatedActivationsKillIt() {
        addMareReady();
        addBlueMana(3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Surge Mare");
        harness.assertInGraveyard(player1, "Surge Mare");
    }

    @Test
    @DisplayName("Surge Mare can't be blocked by a green creature")
    void cannotBeBlockedByGreenCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent mare = addMareReady();
        mare.setAttacking(true);

        beginBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mare);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Surge Mare can be blocked by a non-green creature")
    void canBeBlockedByNonGreenCreature() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent mare = addMareReady();
        mare.setAttacking(true);

        beginBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mare);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Accepting the damage trigger draws then discards a card")
    void damageTriggerLoots() {
        Permanent mare = addMareReady();
        mare.setPowerModifier(2);
        mare.setToughnessModifier(-2);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the damage trigger neither draws nor discards")
    void damageTriggerDeclined() {
        Permanent mare = addMareReady();
        mare.setPowerModifier(2);
        mare.setToughnessModifier(-2);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when Surge Mare deals no damage to a player")
    void noTriggerWithoutDamage() {
        addMareReady();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void addBlueMana(int count) {
        harness.addMana(player1, ManaColor.BLUE, count);
        harness.addMana(player1, ManaColor.COLORLESS, count);
    }

    private Permanent addMareReady() {
        return addCreatureReady(player1, new SurgeMare());
    }
}
