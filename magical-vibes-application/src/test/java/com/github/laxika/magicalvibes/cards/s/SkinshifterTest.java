package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skinshifter.class, TitanicGrowth.class})
class SkinshifterTest extends BaseCardTest {

    @Test
    @DisplayName("Rhino mode makes it a 4/4 Rhino with trample")
    void rhinoMode() {
        Permanent skinshifter = addSkinshifter(player1);

        activate(player1, 0);

        assertThat(gqs.effectiveCreatureSubtypes(gd, skinshifter)).containsExactly(CardSubtype.RHINO);
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Bird mode makes it a 2/2 Bird with flying")
    void birdMode() {
        Permanent skinshifter = addSkinshifter(player1);

        activate(player1, 1);

        assertThat(gqs.effectiveCreatureSubtypes(gd, skinshifter)).containsExactly(CardSubtype.BIRD);
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Plant mode makes it a 0/8 Plant with no granted keyword")
    void plantMode() {
        Permanent skinshifter = addSkinshifter(player1);

        activate(player1, 2);

        assertThat(gqs.effectiveCreatureSubtypes(gd, skinshifter)).containsExactly(CardSubtype.PLANT);
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The type change, base P/T and keyword wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent skinshifter = addSkinshifter(player1);

        activate(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, skinshifter))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SHAMAN);
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void onlyOnceEachTurn() {
        addSkinshifter(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        activate(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An invalid mode is rejected during activation")
    void illegalModeRejected() {
        addSkinshifter(player1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A mode must be chosen before opponents can respond")
    void requiresModeDuringActivation() {
        addSkinshifter(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activation limit applies while the first activation is on the stack")
    void cannotActivateAgainBeforeResolution() {
        addSkinshifter(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, 0, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Skinshifter has its own activation limit")
    void separateCopiesCanActivate() {
        Permanent first = addSkinshifter(player1);
        Permanent second = addSkinshifter(player1);
        activate(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, 1, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can activate on the opponent's turn after activating on its controller's turn")
    void canActivateNextTurn() {
        Permanent skinshifter = addSkinshifter(player1);
        activate(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        activate(player1, 1);

        assertThat(gqs.effectiveCreatureSubtypes(gd, skinshifter)).containsExactly(CardSubtype.BIRD);
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Summoning sickness and being tapped do not prevent activation")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent skinshifter = harness.addToBattlefieldAndReturn(player1, new Skinshifter());
        skinshifter.setSummoningSick(true);
        skinshifter.setTapped(true);

        activate(player1, 2);

        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(8);
        assertThat(skinshifter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Changing base power and toughness preserves an earlier pump effect")
    void preservesPumpEffect() {
        Permanent skinshifter = addSkinshifter(player1);
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, skinshifter.getId());

        activate(player1, 0);

        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(8);
    }

    private Permanent addSkinshifter(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Skinshifter());
        permanent.setSummoningSick(false);
        return permanent;
    }

    /** Pays {G}, chooses the mode during activation and resolves the ability. */
    private void activate(Player player, int mode) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.activateAbility(player, 0, mode, null);
        harness.passBothPriorities();
    }
}
