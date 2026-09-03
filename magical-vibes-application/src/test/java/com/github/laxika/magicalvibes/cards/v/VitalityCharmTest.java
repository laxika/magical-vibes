package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VitalityCharm.class, BarkhideMauler.class, GrizzlyBears.class})
class VitalityCharmTest extends BaseCardTest {

    @Test
    void createsAnInsectToken() {
        harness.setHand(player1, List.of(new VitalityCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getName()).isEqualTo("Insect");
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    void boostsTargetCreatureAndGrantsTrampleUntilEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VitalityCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void regeneratesTargetBeast() {
        Permanent beast = harness.addToBattlefieldAndReturn(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new VitalityCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 2, beast.getId());
        harness.passBothPriorities();

        assertThat(beast.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationModeCannotTargetNonBeastCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VitalityCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
