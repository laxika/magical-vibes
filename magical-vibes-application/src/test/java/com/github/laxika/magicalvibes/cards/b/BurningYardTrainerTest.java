package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.VenerableKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurningYardTrainer.class, VenerableKnight.class})
class BurningYardTrainerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB boosts another Knight you control and grants trample and haste")
    void etbBoostsAnotherKnight() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        castTrainer(knight.getId());

        assertThat(knight.getPowerModifier()).isEqualTo(2);
        assertThat(knight.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("ETB cannot target an opponent's Knight")
    void etbCannotTargetOpponentKnight() {
        Permanent opponentKnight = harness.addToBattlefieldAndReturn(player2, new VenerableKnight());
        harness.setHand(player1, List.of(new BurningYardTrainer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentKnight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another Knight you control");
    }

    @Test
    @DisplayName("ETB cannot target Burning-Yard Trainer itself")
    void etbCannotTargetItself() {
        harness.setHand(player1, List.of(new BurningYardTrainer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost and keyword grants wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        castTrainer(knight.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getPowerModifier()).isZero();
        assertThat(knight.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.HASTE)).isFalse();
    }

    private void castTrainer(UUID targetId) {
        harness.setHand(player1, List.of(new BurningYardTrainer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
