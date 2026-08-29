package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
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

class AdarkarWindformTest extends BaseCardTest {

    @Test
    @DisplayName("Snow ability removes flying from target creature until end of turn")
    void removesFlyingUntilEndOfTurn() {
        addWindformReady(player1);
        Permanent target = addCreatureReady(player2, new WindDrake());
        payAbilityCost(player1);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addWindformReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        addWindformReady(player1);
        Permanent land = new Permanent(new SnowCoveredPlains());
        gd.playerBattlefields.get(player2.getId()).add(land);
        payAbilityCost(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addWindformReady(Player player) {
        return addCreatureReady(player, new AdarkarWindform());
    }

    private void payAbilityCost(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }
}
