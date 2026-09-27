package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
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

@CardUsed({PredatorsStrike.class, AlphaMyr.class, Bonesplitter.class})
class PredatorsStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Predator's Strike gives +3/+3 and trample to target creature")
    void resolvesAllEffects() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.setHand(player1, List.of(new PredatorsStrike()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, myr.getId());
        harness.passBothPriorities();

        assertThat(myr.getPowerModifier()).isEqualTo(3);
        assertThat(myr.getToughnessModifier()).isEqualTo(3);
        assertThat(myr.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Predator's Strike can target an opponent's creature")
    void targetsOpponentsCreature() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new PredatorsStrike()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, myr.getId());
        harness.passBothPriorities();

        assertThat(myr.getPowerModifier()).isEqualTo(3);
        assertThat(myr.getToughnessModifier()).isEqualTo(3);
        assertThat(myr.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Predator's Strike effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.setHand(player1, List.of(new PredatorsStrike()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, myr.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(myr.getPowerModifier()).isZero();
        assertThat(myr.getToughnessModifier()).isZero();
        assertThat(myr.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Predator's Strike cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new AlphaMyr());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        harness.setHand(player1, List.of(new PredatorsStrike()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, equipment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
