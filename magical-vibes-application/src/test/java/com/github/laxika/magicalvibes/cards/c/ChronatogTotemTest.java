package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ChronatogTotem.class)
class ChronatogTotemTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Chronatog Totem adds blue mana")
    void tappingAddsBlueMana() {
        Permanent totem = addReadyTotem();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(totem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Chronatog Totem becomes a blue 1/2 Atog artifact creature")
    void animatesIntoBlueAtog() {
        Permanent totem = addReadyTotem();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isTrue();
        assertThat(gqs.isArtifact(totem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, totem)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, totem)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, totem)).containsExactly(CardColor.BLUE);
        assertThat(totem.getTransientSubtypes()).contains(CardSubtype.ATOG);
    }

    @Test
    @DisplayName("The pump ability boosts the creature, queues a skipped turn, and is once per turn")
    void pumpsAndSkipsNextTurnOncePerTurn() {
        Permanent totem = addReadyTotem();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, totem)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, totem)).isEqualTo(5);
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The pump ability cannot be activated while Chronatog Totem is not a creature")
    void pumpRequiresCreature() {
        addReadyTotem();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a creature");
    }

    @Test
    @DisplayName("Animation and pump effects expire at end of turn")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent totem = addReadyTotem();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isFalse();
        assertThat(totem.getTransientSubtypes()).doesNotContain(CardSubtype.ATOG);
    }

    private Permanent addReadyTotem() {
        Permanent totem = new Permanent(new ChronatogTotem());
        totem.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(totem);
        return totem;
    }
}
