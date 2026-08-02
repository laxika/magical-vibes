package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetropolisSpriteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Metropolis Sprite +1/-1 until end of turn")
    void resolvingAbilityBoostsPowerAndReducesToughness() {
        Permanent sprite = addReadyMetropolisSprite(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sprite.getPowerModifier()).isEqualTo(1);
        assertThat(sprite.getToughnessModifier()).isEqualTo(-1);
        assertThat(sprite.getEffectivePower()).isEqualTo(2);
        assertThat(sprite.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The temporary boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent sprite = addReadyMetropolisSprite(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(sprite.getPowerModifier()).isEqualTo(1);
        assertThat(sprite.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sprite.getPowerModifier()).isEqualTo(0);
        assertThat(sprite.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability cannot be activated without blue mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyMetropolisSprite(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyMetropolisSprite(Player player) {
        Permanent sprite = new Permanent(new MetropolisSprite());
        sprite.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sprite);
        return sprite;
    }
}
