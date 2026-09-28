package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WolfsbaneHighlandHero.class)
class WolfsbaneHighlandHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability grants +2/+2 until end of turn")
    void activatedAbilityBoosts() {
        Permanent wolfsbane = addReadyWolfsbane(player1);
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolfsbane.getEffectivePower()).isEqualTo(4);
        assertThat(wolfsbane.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Activated ability can only be used once each turn")
    void activatedAbilityIsLimitedToOncePerTurn() {
        addReadyWolfsbane(player1);
        addManaForAbility(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Activated ability boost wears off at end of turn")
    void activatedAbilityBoostWearsOffAtEndOfTurn() {
        Permanent wolfsbane = addReadyWolfsbane(player1);
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolfsbane.getEffectivePower()).isEqualTo(2);
        assertThat(wolfsbane.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyWolfsbane(Player player) {
        Permanent permanent = new Permanent(new WolfsbaneHighlandHero());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addManaForAbility(Player player) {
        addManaForAbility(player, 1);
    }

    private void addManaForAbility(Player player, int activations) {
        harness.addMana(player, ManaColor.GREEN, activations);
        harness.addMana(player, ManaColor.COLORLESS, activations * 2);
    }
}
