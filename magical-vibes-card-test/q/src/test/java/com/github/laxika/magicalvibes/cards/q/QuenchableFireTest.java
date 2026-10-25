package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DamageAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuenchableFire.class, ElspethKnightErrant.class, CylianElf.class})
class QuenchableFireTest extends BaseCardTest {

    // "Quenchable Fire deals 3 damage to target player or planeswalker. It deals an additional
    //  3 damage to that player or planeswalker at the beginning of your next upkeep step unless
    //  that player or that planeswalker's controller pays {U} before that step."

    private void giveQuenchableFire() {
        harness.setHand(player1, List.of(new QuenchableFire()));
        harness.addMana(player1, ManaColor.RED, 4);
    }

    /** Casts Quenchable Fire at {@code targetId}, resolves it, and hands priority to player2. */
    private QuenchableFire castAtAndPassToPlayer2(java.util.UUID targetId) {
        QuenchableFire fire = new QuenchableFire();
        harness.setHand(player1, List.of(fire));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passPriority(player1);
        return fire;
    }

    /** Resolve the delayed ability at the caster's next upkeep. */
    private void resolveNextUpkeep() {
        gd.turnNumber = 3;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 3 damage now and schedules the delayed obligation against the target")
    void immediateDamageAndScheduledObligation() {
        giveQuenchableFire();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);

        List<DamageAtNextUpkeepUnlessPays> scheduled = gd.getDelayedActions(DamageAtNextUpkeepUnlessPays.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().spellControllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().targetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Without prior payment, the additional damage is dealt at the caster's next upkeep")
    void noPriorPaymentDealsAdditionalDamage() {
        giveQuenchableFire();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        int lifeAfterImmediate = gd.getLife(player2.getId());

        resolveNextUpkeep();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeAfterImmediate - 3);
        assertThat(gd.getDelayedActions(DamageAtNextUpkeepUnlessPays.class)).isEmpty();
    }

    @Test
    @DisplayName("Once upkeep begins, paying {U} can no longer prevent the additional damage")
    void cannotPayAfterUpkeepBegins() {
        giveQuenchableFire();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        int lifeAfterImmediate = gd.getLife(player2.getId());

        gd.turnNumber = 3;
        advanceToUpkeep(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeAfterImmediate - 3);
    }

    @Test
    @DisplayName("A planeswalker loses 3 more loyalty without payment before upkeep")
    void planeswalkerWithoutPriorPaymentLosesLoyalty() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 8);
        giveQuenchableFire();

        harness.castSorcery(player1, 0, elspeth.getId());
        harness.passBothPriorities();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 8 - 3

        resolveNextUpkeep();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 5 - 3
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new CylianElf());
        giveQuenchableFire();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Cylian Elf")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The caster controls the delayed damage ability")
    void casterControlsDelayedAbility() {
        giveQuenchableFire();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        gd.turnNumber = 3;
        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The opponent's upkeep does not trigger the delayed damage")
    void opponentUpkeepDoesNotTriggerDamage() {
        giveQuenchableFire();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        int lifeAfterImmediate = gd.getLife(player2.getId());

        gd.turnNumber = 3;
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeAfterImmediate);
        assertThat(gd.getDelayedActions(DamageAtNextUpkeepUnlessPays.class)).hasSize(1);
    }
    @Test
    @DisplayName("Paying {U} before the caster's upkeep stops the additional damage")
    void payingBeforeUpkeepPreventsAdditionalDamage() {
        QuenchableFire fire = castAtAndPassToPlayer2(player2.getId());
        int lifeAfterImmediate = gd.getLife(player2.getId());
        harness.addMana(player2, ManaColor.BLUE, 1);

        gs.payDelayedTriggerCost(gd, player2, fire.getId(), player2.getId());

        assertThat(gd.getDelayedActions(DamageAtNextUpkeepUnlessPays.class)).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();

        gd.turnNumber = 3;
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeAfterImmediate);
    }

    @Test
    @DisplayName("The payment is offered to the damaged player while they have priority, and not to the caster")
    void paymentOfferedOnlyToDamagedPlayerWithPriority() {
        QuenchableFire fire = castAtAndPassToPlayer2(player2.getId());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        var availability = harness.getGameActionAvailabilityService();

        assertThat(availability.getAvailableDelayedTriggerPayments(gd, player2.getId()))
                .singleElement()
                .satisfies(payment -> {
                    assertThat(payment.sourceCard().getId()).isEqualTo(fire.getId());
                    assertThat(payment.affectedId()).isEqualTo(player2.getId());
                    assertThat(payment.manaCost()).isEqualTo("{U}");
                });
        assertThat(availability.getAvailableDelayedTriggerPayments(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The payment is not offered to the damaged player while the caster holds priority")
    void paymentNotOfferedWithoutPriority() {
        giveQuenchableFire();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player1.getId());
        assertThat(harness.getGameActionAvailabilityService()
                .getAvailableDelayedTriggerPayments(gd, player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The caster can't pay to stop their own delayed damage")
    void casterCannotPay() {
        giveQuenchableFire();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card fire = gd.playerGraveyards.get(player1.getId()).getFirst();

        assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player1.getId());
        assertThatThrownBy(() -> gs.payDelayedTriggerCost(gd, player1, fire.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No unpaid");
        assertThat(gd.getDelayedActions(DamageAtNextUpkeepUnlessPays.class)).hasSize(1);
    }

    @Test
    @DisplayName("Paying requires priority")
    void payingRequiresPriority() {
        giveQuenchableFire();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.BLUE, 1);
        Card fire = gd.playerGraveyards.get(player1.getId()).getFirst();

        assertThatThrownBy(() -> gs.payDelayedTriggerCost(gd, player2, fire.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getDelayedActions(DamageAtNextUpkeepUnlessPays.class)).hasSize(1);
    }

    @Test
    @DisplayName("Paying requires blue mana; a failed payment keeps the obligation and the mana")
    void payingRequiresBlueMana() {
        QuenchableFire fire = castAtAndPassToPlayer2(player2.getId());
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.payDelayedTriggerCost(gd, player2, fire.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.getDelayedActions(DamageAtNextUpkeepUnlessPays.class)).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying is a special action: the payer keeps priority and an earlier pass no longer counts")
    void payingIsASpecialAction() {
        QuenchableFire fire = castAtAndPassToPlayer2(player2.getId());
        harness.addMana(player2, ManaColor.BLUE, 1);
        TurnStep stepBefore = gd.currentStep;

        gs.payDelayedTriggerCost(gd, player2, fire.getId(), player2.getId());

        assertThat(gd.currentStep).isEqualTo(stepBefore);
        assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player2.getId());
        assertThat(gd.priorityPassedBy).isEmpty();
    }

    @Test
    @DisplayName("The damaged planeswalker's controller can pay to stop the additional damage")
    void planeswalkerControllerCanPay() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 8);
        QuenchableFire fire = castAtAndPassToPlayer2(elspeth.getId());
        harness.addMana(player2, ManaColor.BLUE, 1);

        gs.payDelayedTriggerCost(gd, player2, fire.getId(), elspeth.getId());

        gd.turnNumber = 3;
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }
}
