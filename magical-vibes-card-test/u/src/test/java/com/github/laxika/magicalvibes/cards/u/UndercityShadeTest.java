package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.g.GolgariThug;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndercityShade.class, BorosRecruit.class, GlassGolem.class, GolgariThug.class})
class UndercityShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {B} gives Undercity Shade +1/+1 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent shade = addReadyShade(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Undercity Shade can activate its non-tapping ability more than once per turn")
    void activatedAbilityCanBeUsedMultipleTimes() {
        Permanent shade = addReadyShade(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(2);
        assertThat(shade.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Undercity Shade's temporary boost wears off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent shade = addReadyShade(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isZero();
        assertThat(shade.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Fear prevents nonblack nonartifact creatures from blocking Undercity Shade")
    void fearPreventsNonblackNonartifactCreatureFromBlocking() {
        Permanent shade = addReadyShade(player1);
        shade.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BorosRecruit());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(shade)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows black and artifact creatures to block Undercity Shade")
    void fearAllowsBlackAndArtifactCreaturesToBlock() {
        Permanent shade = addReadyShade(player1);
        shade.setAttacking(true);
        Permanent blackBlocker = addCreatureReady(player2, new GolgariThug());
        Permanent artifactBlocker = addCreatureReady(player2, new GlassGolem());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blackBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(shade)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(artifactBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(shade))));

        assertThat(blackBlocker.isBlocking()).isTrue();
        assertThat(artifactBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Undercity Shade can pump while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent shade = addReadyShade(player1);
        shade.setSummoningSick(true);
        shade.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
        assertThat(shade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Stacked activations boost only the Shade that activated them")
    void stackedActivationsBoostOnlyTheirSource() {
        Permanent otherShade = addReadyShade(player1);
        Permanent source = addReadyShade(player1);
        Permanent opposingShade = addReadyShade(player2);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.activateAbility(player1, 1, null, null);

        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
        resolveAllTriggers();

        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.getToughnessModifier()).isEqualTo(2);
        assertThat(otherShade.getPowerModifier()).isZero();
        assertThat(otherShade.getToughnessModifier()).isZero();
        assertThat(opposingShade.getPowerModifier()).isZero();
        assertThat(opposingShade.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Nonblack mana cannot pay Undercity Shade's activation cost")
    void activationRequiresBlackMana() {
        Permanent shade = addReadyShade(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(shade.getPowerModifier()).isZero();
        assertThat(shade.getToughnessModifier()).isZero();
    }

    private Permanent addReadyShade(Player player) {
        Permanent shade = addCreatureReady(player, new UndercityShade());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return shade;
    }
}
