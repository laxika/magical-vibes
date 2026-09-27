package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.r.RelicBarrier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PixieQueen.class, DurkwoodBoars.class, RelicBarrier.class})
class PixieQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability grants flying to target creature")
    void resolvingGrantsFlying() {
        Permanent pixieQueen = addCreatureReady(player1, new PixieQueen());
        Permanent target = addCreatureReady(player1, new DurkwoodBoars());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(pixieQueen.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        addCreatureReady(player1, new PixieQueen());
        Permanent target = addCreatureReady(player2, new DurkwoodBoars());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying is removed at end of turn")
    void flyingRemovedAtEndOfTurn() {
        addCreatureReady(player1, new PixieQueen());
        Permanent target = addCreatureReady(player1, new DurkwoodBoars());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new PixieQueen());
        Permanent target = addCreatureReady(player1, new DurkwoodBoars());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent pixieQueen = addCreatureReady(player1, new PixieQueen());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RelicBarrier());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(pixieQueen.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent pixieQueen = addCreatureReady(player1, new PixieQueen());
        Permanent target = addCreatureReady(player1, new DurkwoodBoars());
        pixieQueen.tap();
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
