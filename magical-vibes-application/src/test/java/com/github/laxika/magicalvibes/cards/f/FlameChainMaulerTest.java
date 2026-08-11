package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlameChainMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+0 and menace until end of turn")
    void resolvingAbilityBoostsAndGrantsMenace() {
        Permanent mauler = addReadyMauler(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The boost and menace wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent mauler = addReadyMauler(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Activating the ability does not tap Flame-Chain Mauler")
    void activatingDoesNotTap() {
        Permanent mauler = addReadyMauler(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mauler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability requires one red and one generic mana")
    void requiresRedAndGenericMana() {
        addReadyMauler(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMauler(Player player) {
        Permanent perm = new Permanent(new FlameChainMauler());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
