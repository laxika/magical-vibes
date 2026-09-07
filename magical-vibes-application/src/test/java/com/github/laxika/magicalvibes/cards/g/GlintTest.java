package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({Glint.class, GrizzlyBears.class, GiantGrowth.class})
class GlintTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature you control +0/+3 and hexproof")
    void boostsAndGrantsHexproof() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(creature);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("The toughness boost and hexproof wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Glint()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting the protected creature")
    void hexproofPreventsOpponentTargeting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(creature);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new Glint()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
