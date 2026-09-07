package com.github.laxika.magicalvibes.cards.k;

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

@CardUsed(KravensCats.class)
class KravensCatsTest extends BaseCardTest {

    @Test
    @DisplayName("Pump ability grants +2/+2 until end of turn")
    void pumpAbilityGrantsBoost() {
        Permanent cats = addReadyCats(player1);
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cats)).isEqualTo(4);
    }

    @Test
    @DisplayName("Pump ability can be activated only once each turn")
    void pumpAbilityOncePerTurn() {
        addReadyCats(player1);
        addManaForAbility(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent cats = addReadyCats(player1);
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, cats)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cats)).isEqualTo(2);
    }

    private Permanent addReadyCats(Player player) {
        Permanent perm = new Permanent(new KravensCats());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addManaForAbility(Player player) {
        addManaForAbility(player, 1);
    }

    private void addManaForAbility(Player player, int activations) {
        harness.addMana(player, ManaColor.GREEN, activations);
        harness.addMana(player, ManaColor.COLORLESS, activations * 2);
    }
}
