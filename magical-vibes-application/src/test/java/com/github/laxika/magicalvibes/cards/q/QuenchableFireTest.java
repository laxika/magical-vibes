package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
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
}
