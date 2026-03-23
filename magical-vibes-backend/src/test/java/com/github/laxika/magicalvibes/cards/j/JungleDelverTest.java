package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JungleDelverTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has one activated ability with {3}{G} mana cost and no tap requirement")
    void hasCorrectAbility() {
        JungleDelver card = new JungleDelver();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{3}{G}");
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(PutCounterOnSelfEffect.class);

        PutCounterOnSelfEffect effect = (PutCounterOnSelfEffect)
                card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(effect.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent delver = addReadyDelver(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability requires {3}{G} mana")
    void requiresMana() {
        Permanent delver = addReadyDelver(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Resolving ability =====

    @Test
    @DisplayName("Resolving puts a +1/+1 counter on Jungle Delver")
    void resolvingPutsCounter() {
        Permanent delver = addReadyDelver(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(delver.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating ability twice puts two +1/+1 counters")
    void activatingTwicePutsTwoCounters() {
        Permanent delver = addReadyDelver(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(delver.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    // ===== No tap required — can activate while summoning sick =====

    @Test
    @DisplayName("Can activate while summoning sick because ability does not require tap")
    void canActivateWhileSummoningSick() {
        Permanent delver = new Permanent(new JungleDelver());
        delver.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(delver);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(delver.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadyDelver(Player player) {
        Permanent perm = new Permanent(new JungleDelver());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
