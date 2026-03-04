package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShriekhornTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect for entering with 3 charge counters")
    void hasEnterWithChargeCountersEffect() {
        Shriekhorn card = new Shriekhorn();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithFixedChargeCountersEffect.class);
        EnterWithFixedChargeCountersEffect effect = (EnterWithFixedChargeCountersEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Has activated ability: tap + remove charge counter to mill target player 2 cards")
    void hasActivatedAbility() {
        Shriekhorn card = new Shriekhorn();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 1)
                .anyMatch(e -> e instanceof MillTargetPlayerEffect mp && mp.count() == 2);
    }

    // ===== Entering the battlefield with charge counters =====

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new Shriekhorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent shriekhorn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shriekhorn"))
                .findFirst().orElseThrow();
        assertThat(shriekhorn.getChargeCounters()).isEqualTo(3);
    }

    // ===== Activated ability: target player mills two cards =====

    @Test
    @DisplayName("Activating ability removes a charge counter and target player mills 2 cards")
    void activateRemovesCounterAndTargetMills() {
        harness.addToBattlefield(player1, new Shriekhorn());

        Permanent shriekhorn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shriekhorn"))
                .findFirst().orElseThrow();
        shriekhorn.setChargeCounters(3);

        int initialLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(shriekhorn.getChargeCounters()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(initialLibrarySize - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can activate three times with 3 charge counters (untapping between uses)")
    void canActivateThreeTimes() {
        harness.addToBattlefield(player1, new Shriekhorn());

        Permanent shriekhorn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shriekhorn"))
                .findFirst().orElseThrow();
        shriekhorn.setChargeCounters(3);

        int initialLibrarySize = gd.playerDecks.get(player2.getId()).size();

        // First activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        shriekhorn.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        shriekhorn.untap();

        // Third activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(shriekhorn.getChargeCounters()).isEqualTo(0);
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(initialLibrarySize - 6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Cannot activate with 0 charge counters")
    void cannotActivateWithNoCounters() {
        harness.addToBattlefield(player1, new Shriekhorn());

        Permanent shriekhorn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shriekhorn"))
                .findFirst().orElseThrow();
        shriekhorn.setChargeCounters(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target yourself to mill your own cards")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new Shriekhorn());

        Permanent shriekhorn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shriekhorn"))
                .findFirst().orElseThrow();
        shriekhorn.setChargeCounters(1);

        int initialLibrarySize = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(shriekhorn.getChargeCounters()).isEqualTo(0);
        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(initialLibrarySize - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate while tapped (requires tap)")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new Shriekhorn());

        Permanent shriekhorn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shriekhorn"))
                .findFirst().orElseThrow();
        shriekhorn.setChargeCounters(3);

        // First activation taps it
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(shriekhorn.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
