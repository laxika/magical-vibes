package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SavedByTheShell.class, GrizzlyBears.class, HornedTurtle.class})
class SavedByTheShellTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the target and grants trample, hexproof, and indestructible")
    void putsCounterAndGrantsKeywords() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castSavedByTheShell(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Temporary keyword grants expire at end of turn but the counter remains")
    void keywordsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castSavedByTheShell(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Costs one generic mana less when its controller controls a Turtle")
    void turtleReducesCost() {
        harness.addToBattlefield(player1, new HornedTurtle());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavedByTheShell()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast for only one mana without controlling a Turtle")
    void needsGenericManaWithoutTurtle() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavedByTheShell()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavedByTheShell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void castSavedByTheShell(Permanent target) {
        harness.setHand(player1, List.of(new SavedByTheShell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
