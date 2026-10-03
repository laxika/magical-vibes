package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.f.FormOfTheDragon;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Capricopian.class, NicolBolasPlaneswalker.class, FormOfTheDragon.class})
class CapricopianTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new Capricopian()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Capricopian")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can't attack its current controller")
    void cantAttackCurrentController() {
        Capricopian card = new Capricopian();
        card.setOwnerId(player1.getId());
        Permanent capricopian = addCreatureReady(player2, card);

        assertThat(als.canAttackDefender(gd, capricopian, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, capricopian, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Only the player it attacks can activate the declare-attackers ability")
    void onlyAttackedPlayerCanActivate() {
        Permanent capricopian = addAttackingCapricopian();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only the player this creature is attacking");
        assertThat(capricopian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attacked player may add a counter and reselect only a player")
    void attackedPlayerCanReselectPlayerOnly() {
        Permanent capricopian = addAttackingCapricopian();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).isEmpty();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, planeswalker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player2, player2.getId());

        assertThat(capricopian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("X zero enters without counters and dies")
    void zeroXDies() {
        harness.setHand(player1, List.of(new Capricopian()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Capricopian");
        harness.assertInGraveyard(player1, "Capricopian");
    }

    @Test
    @DisplayName("Declining reselection still adds a counter and keeps the defender")
    void declineReselectionStillAddsCounter() {
        Permanent capricopian = addAttackingCapricopian();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(capricopian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(capricopian.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The attacked player cannot activate during declare blockers")
    void cannotActivateDuringDeclareBlockers() {
        Permanent capricopian = addAttackingCapricopian();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only the player this creature is attacking");
        assertThat(capricopian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No player may activate while Capricopian attacks a planeswalker")
    void cannotActivateWhenAttackingPlaneswalker() {
        harness.addToBattlefield(player1, new NicolBolasPlaneswalker());
        Permanent capricopian = addAttackingCapricopian();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        capricopian.setAttackTarget(planeswalker.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only the player this creature is attacking");
        assertThat(capricopian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A nonattacking Capricopian cannot grant the defending player an activation")
    void cannotActivateWhenNotAttacking() {
        Permanent capricopian = addAttackingCapricopian();
        capricopian.setAttacking(false);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only the player this creature is attacking");
        assertThat(capricopian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reselecting the current defender ignores attack restrictions")
    void reselectionIgnoresAttackRestrictions() {
        Permanent capricopian = addAttackingCapricopian();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.addToBattlefield(player2, new FormOfTheDragon());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player2, player2.getId());
        assertThat(capricopian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(capricopian.getAttackTarget()).isEqualTo(player2.getId());
    }

    private Permanent addAttackingCapricopian() {
        Permanent capricopian = addCreatureReady(player1, new Capricopian());
        capricopian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        capricopian.setAttacking(true);
        capricopian.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        return capricopian;
    }
}
