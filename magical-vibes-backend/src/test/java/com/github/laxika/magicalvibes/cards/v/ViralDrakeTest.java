package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViralDrakeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has proliferate activated ability that does not require tap")
    void hasProliferateAbilityWithoutTap() {
        ViralDrake card = new ViralDrake();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).singleElement()
                .isInstanceOf(ProliferateEffect.class);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{3}{U}");
    }

    // ===== Proliferate activated ability =====

    @Test
    @DisplayName("Proliferate ability adds -1/-1 counter to creature that already has one")
    void proliferateAddsMinusCounters() {
        Permanent drake = addReadyDrake(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate ability can be activated multiple times per turn (no tap required)")
    void canActivateMultipleTimesPerTurn() {
        Permanent drake = addReadyDrake(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // First activation
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(2);

        // Second activation — re-force step to ensure priority state is clean
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Proliferate adds +1/+1 counter to creature that already has one")
    void proliferateAddsPlusCounters() {
        Permanent drake = addReadyDrake(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        Permanent drake = addReadyDrake(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadyDrake(Player player) {
        ViralDrake card = new ViralDrake();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
