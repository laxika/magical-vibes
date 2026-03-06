package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EtchedMonstrosityTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB five -1/-1 counters and one activated ability")
    void hasCorrectEffectsAndAbility() {
        EtchedMonstrosity card = new EtchedMonstrosity();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(PutCountersOnSourceEffect.class);

        PutCountersOnSourceEffect etb = (PutCountersOnSourceEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etb.powerModifier()).isEqualTo(-1);
        assertThat(etb.toughnessModifier()).isEqualTo(-1);
        assertThat(etb.amount()).isEqualTo(5);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(2)
                .satisfies(effects -> {
                    assertThat(effects.get(0)).isInstanceOf(RemoveCounterFromSourceCost.class);
                    assertThat(((RemoveCounterFromSourceCost) effects.get(0)).count()).isEqualTo(5);
                    assertThat(effects.get(1)).isInstanceOf(DrawCardForTargetPlayerEffect.class);
                    assertThat(((DrawCardForTargetPlayerEffect) effects.get(1)).amount()).isEqualTo(3);
                });
    }

    // ===== ETB: enters with five -1/-1 counters =====

    @Test
    @DisplayName("Enters the battlefield with five -1/-1 counters (10/10 becomes 5/5)")
    void entersWithFiveMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EtchedMonstrosity()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        Permanent monstrosity = findMonstrosity(player1);

        assertThat(monstrosity.getMinusOneMinusOneCounters()).isEqualTo(5);
        assertThat(monstrosity.getEffectivePower()).isEqualTo(5);
        assertThat(monstrosity.getEffectiveToughness()).isEqualTo(5);
    }

    // ===== Activated ability: target player draws three cards =====

    @Test
    @DisplayName("Activated ability makes target player draw three cards")
    void abilityTargetPlayerDrawsThreeCards() {
        Permanent monstrosity = addReadyMonstrosity(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Activated ability can target self to draw three cards")
    void abilityCanTargetSelf() {
        Permanent monstrosity = addReadyMonstrosity(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Activated ability removes all five -1/-1 counters as cost")
    void abilityRemovesFiveCounters() {
        Permanent monstrosity = addReadyMonstrosity(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(monstrosity.getMinusOneMinusOneCounters()).isEqualTo(0);
        assertThat(monstrosity.getEffectivePower()).isEqualTo(10);
        assertThat(monstrosity.getEffectiveToughness()).isEqualTo(10);
    }

    // ===== Cannot activate without enough counters =====

    @Test
    @DisplayName("Cannot activate ability when fewer than five counters remain")
    void cannotActivateWithoutEnoughCounters() {
        Permanent monstrosity = addReadyMonstrosity(player1);
        monstrosity.setMinusOneMinusOneCounters(4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot activate ability when zero counters remain")
    void cannotActivateWithZeroCounters() {
        Permanent monstrosity = addReadyMonstrosity(player1);
        monstrosity.setMinusOneMinusOneCounters(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot activate ability with +1/+1 counters instead of -1/-1 counters")
    void cannotActivateWithPlusCounters() {
        Permanent monstrosity = addReadyMonstrosity(player1);
        monstrosity.setMinusOneMinusOneCounters(0);
        monstrosity.setPlusOnePlusOneCounters(5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    // ===== Helpers =====

    private Permanent addReadyMonstrosity(Player player) {
        EtchedMonstrosity card = new EtchedMonstrosity();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setMinusOneMinusOneCounters(5);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findMonstrosity(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Etched Monstrosity"))
                .findFirst().orElseThrow();
    }

    private void addWUBRGMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
