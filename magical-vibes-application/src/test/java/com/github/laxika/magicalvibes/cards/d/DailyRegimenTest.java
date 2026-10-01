package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.m.MurmuringBosk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DailyRegimen.class, ElvishWarrior.class, MurmuringBosk.class})
class DailyRegimenTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Daily Regimen attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creaturePerm = addCreatureReady(player1, new ElvishWarrior());

        harness.setHand(player1, List.of(new DailyRegimen()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creaturePerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Daily Regimen")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creaturePerm.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());

        harness.setHand(player1, List.of(new DailyRegimen()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Activated ability: +1/+1 counter on enchanted creature =====

    @Test
    @DisplayName("Activating ability puts a +1/+1 counter on the enchanted creature")
    void activatingAbilityAddsCounter() {
        Permanent creaturePerm = addCreatureReady(player1, new ElvishWarrior());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new DailyRegimen());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Aura is at index 1 (bears at 0, aura at 1)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creaturePerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack as an activated ability")
    void activatingAbilityPutsOnStack() {
        Permanent creaturePerm = addCreatureReady(player1, new ElvishWarrior());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new DailyRegimen());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Daily Regimen");
    }

    @Test
    @DisplayName("Counters accumulate over multiple activations")
    void countersAccumulate() {
        Permanent creaturePerm = addCreatureReady(player1, new ElvishWarrior());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new DailyRegimen());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creaturePerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    // ===== Can enchant opponent's creature =====

    @Test
    @DisplayName("Can enchant opponent's creature and boost it")
    void canEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new ElvishWarrior());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new DailyRegimen());
        auraPerm.setAttachedTo(opponentCreature.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Aura is at index 0 on player1's battlefield (only permanent)
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
