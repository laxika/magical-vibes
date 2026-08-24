package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningCoyote.class, GrizzlyBears.class})
class CunningCoyoteTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another creature you control +1/+1 and haste")
    void etbBoostsAndGrantsHaste() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("ETB boost and haste wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("ETB targets another Coyote rather than itself")
    void etbTargetsAnotherCoyote() {
        Permanent existingCoyote = harness.addToBattlefieldAndReturn(player1, new CunningCoyote());
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, existingCoyote.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(existingCoyote.getPowerModifier()).isEqualTo(1);
        assertThat(existingCoyote.getToughnessModifier()).isEqualTo(1);
        assertThat(existingCoyote.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(existingCoyote.getId()))
                .singleElement()
                .satisfies(coyote -> {
                    assertThat(coyote.getPowerModifier()).isZero();
                    assertThat(coyote.getToughnessModifier()).isZero();
                    assertThat(coyote.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
                });
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    @DisplayName("Can be cast without a target when no other creatures are controlled")
    void canBeCastWithoutTarget() {
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
