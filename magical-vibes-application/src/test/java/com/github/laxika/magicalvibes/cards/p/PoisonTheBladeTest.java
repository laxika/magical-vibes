package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

@CardUsed({PoisonTheBlade.class, GrizzlyBears.class, FountainOfYouth.class})
class PoisonTheBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature deathtouch and draws a card")
    void givesDeathtouchAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PoisonTheBlade()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSpellMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PoisonTheBlade()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSpellMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new PoisonTheBlade()));
        addSpellMana();

        Permanent artifact = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
