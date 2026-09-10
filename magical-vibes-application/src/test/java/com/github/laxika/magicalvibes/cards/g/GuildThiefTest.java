package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuildThief.class, GrizzlyBears.class})
class GuildThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when it deals combat damage to a player")
    void putsCounterOnItselfAfterCombatDamage() {
        Permanent thief = addReadyGuildThief();
        thief.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(thief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself when it is blocked")
    void doesNotPutCounterWhenBlocked() {
        Permanent thief = addReadyGuildThief();
        thief.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(thief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Resolving the ability makes Guild Thief unblockable for the turn")
    void abilityMakesItUnblockable() {
        Permanent thief = addReadyGuildThief();
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(thief.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off during cleanup")
    void unblockableWearsOff() {
        Permanent thief = addReadyGuildThief();
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thief.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The ability requires four mana")
    void abilityRequiresMana() {
        addReadyGuildThief();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGuildThief() {
        Permanent thief = harness.addToBattlefieldAndReturn(player1, new GuildThief());
        thief.setSummoningSick(false);
        return thief;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
