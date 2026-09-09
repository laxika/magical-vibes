package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicBlessing.class, AlabornTrooper.class, Forest.class})
class AngelicBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +3/+3 and gains flying until end of turn")
    void boostsAndGrantsFlyingUntilEndOfTurn() {
        Permanent target = castAngelicBlessing(addCreatureReady(player1, new AlabornTrooper()));

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The boost and flying expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = castAngelicBlessing(addCreatureReady(player1, new AlabornTrooper()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent target = castAngelicBlessing(addCreatureReady(player2, new AlabornTrooper()));

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AngelicBlessing()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot cast without a creature target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new AngelicBlessing()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castAngelicBlessing(Permanent target) {
        harness.setHand(player1, List.of(new AngelicBlessing()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        return target;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
