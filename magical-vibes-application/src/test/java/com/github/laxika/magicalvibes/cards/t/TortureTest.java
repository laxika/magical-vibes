package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TortureTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Torture attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Torture()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Torture")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Activating ability puts a -1/-1 counter on the enchanted creature")
    void activatingPutsMinusCounter() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new Torture());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Aura at index 1 (bears at 0, aura at 1)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(bearsPerm.getEffectivePower()).isEqualTo(1);
        assertThat(bearsPerm.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new Torture());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Torture");
    }

    @Test
    @DisplayName("Ability can be activated multiple times, stacking -1/-1 counters")
    void abilityStacksCounters() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new Torture());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can enchant an opponent's creature and shrink it")
    void canEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new Torture());
        auraPerm.setAttachedTo(opponentCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Aura at index 0 on player1's battlefield (only permanent)
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}
