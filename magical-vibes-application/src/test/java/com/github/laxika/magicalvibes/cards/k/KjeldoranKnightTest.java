package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KjeldoranKnightTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{W} ability gives +1/+0 until end of turn")
    void powerBoost() {
        addKnightReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent knight = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("{W}{W} ability gives +0/+2 until end of turn")
    void toughnessBoost() {
        addKnightReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent knight = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(1);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Both boosts stack and wear off at end of turn")
    void bothBoostsWearOff() {
        addKnightReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent knight = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(1);
        assertThat(knight.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate power boost without mana")
    void cannotActivateWithoutMana() {
        addKnightReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addKnightReady(Player player) {
        KjeldoranKnight card = new KjeldoranKnight();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
