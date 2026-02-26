package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecrogenCenserTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect for entering with 2 charge counters")
    void hasEnterWithChargeCountersEffect() {
        NecrogenCenser card = new NecrogenCenser();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithFixedChargeCountersEffect.class);
        EnterWithFixedChargeCountersEffect effect = (EnterWithFixedChargeCountersEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Has activated ability: tap + remove charge counter to target player loses 2 life")
    void hasActivatedAbility() {
        NecrogenCenser card = new NecrogenCenser();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 1)
                .anyMatch(e -> e instanceof TargetPlayerLosesLifeAndControllerGainsLifeEffect tp
                        && tp.lifeLoss() == 2 && tp.lifeGain() == 0);
    }

    // ===== Entering the battlefield with charge counters =====

    @Test
    @DisplayName("Enters the battlefield with 2 charge counters")
    void entersWithTwoChargeCounters() {
        harness.setHand(player1, List.of(new NecrogenCenser()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent censer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Necrogen Censer"))
                .findFirst().orElseThrow();
        assertThat(censer.getChargeCounters()).isEqualTo(2);
    }

    // ===== Activated ability: target player loses 2 life =====

    @Test
    @DisplayName("Activating ability removes a charge counter and target player loses 2 life")
    void activateRemovesCounterAndTargetLosesLife() {
        harness.addToBattlefield(player1, new NecrogenCenser());

        Permanent censer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Necrogen Censer"))
                .findFirst().orElseThrow();
        censer.setChargeCounters(2);

        int initialLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(censer.getChargeCounters()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife - 2);
    }

    @Test
    @DisplayName("Can activate twice with 2 charge counters (untapping between uses)")
    void canActivateTwice() {
        harness.addToBattlefield(player1, new NecrogenCenser());

        Permanent censer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Necrogen Censer"))
                .findFirst().orElseThrow();
        censer.setChargeCounters(2);

        int initialLife = gd.playerLifeTotals.get(player2.getId());

        // First activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        censer.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(censer.getChargeCounters()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife - 4);
    }

    @Test
    @DisplayName("Cannot activate with 0 charge counters")
    void cannotActivateWithNoCounters() {
        harness.addToBattlefield(player1, new NecrogenCenser());

        Permanent censer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Necrogen Censer"))
                .findFirst().orElseThrow();
        censer.setChargeCounters(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target yourself to lose life")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new NecrogenCenser());

        Permanent censer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Necrogen Censer"))
                .findFirst().orElseThrow();
        censer.setChargeCounters(1);

        int initialLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(censer.getChargeCounters()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(initialLife - 2);
    }

    @Test
    @DisplayName("Cannot activate while tapped (requires tap)")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new NecrogenCenser());

        Permanent censer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Necrogen Censer"))
                .findFirst().orElseThrow();
        censer.setChargeCounters(2);

        // First activation taps it
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(censer.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
