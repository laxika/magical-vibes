package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NinjaOfTheDeepHours.class, GnarledMass.class})
class NinjaOfTheDeepHoursTest extends BaseCardTest {

    @Test
    @DisplayName("May draw a card when the Ninja deals combat damage to a player")
    void mayDrawOnCombatDamage() {
        Permanent ninja = addCreatureReady(player1, new NinjaOfTheDeepHours());
        ninja.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new GnarledMass(), new GnarledMass())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the trigger draws no card")
    void decliningDrawsNothing() {
        Permanent ninja = addCreatureReady(player1, new NinjaOfTheDeepHours());
        ninja.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new GnarledMass(), new GnarledMass())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No trigger when the Ninja is blocked and deals no damage to the player")
    void noTriggerWhenBlocked() {
        Permanent ninja = addCreatureReady(player1, new NinjaOfTheDeepHours());
        ninja.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GnarledMass());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts the Ninja in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new GnarledMass());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NinjaOfTheDeepHours()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gnarled Mass");
        Permanent ninja = findPermanent(player1, "Ninja of the Deep Hours");
        assertThat(ninja.isTapped()).isTrue();
        assertThat(ninja.isAttacking()).isTrue();
        assertThat(ninja.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Ninjutsu cannot return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new GnarledMass());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NinjaOfTheDeepHours()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
    }
}
