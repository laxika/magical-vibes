package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GruulNodorog;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.h.HatchingPlans;
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

@CardUsed({Wildsize.class, GruulNodorog.class, GruulSignet.class, HatchingPlans.class})
class WildsizeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts target creature, grants trample, and draws a card")
    void boostsGrantsTrampleAndDraws() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GruulNodorog());
        harness.setLibrary(player1, List.of(new HatchingPlans()));
        harness.setHand(player1, List.of(new Wildsize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The boost and trample wear off at cleanup")
    void temporaryEffectsWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GruulNodorog());
        harness.setLibrary(player1, List.of(new HatchingPlans()));
        harness.setHand(player1, List.of(new Wildsize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Does not draw when the target is illegal on resolution")
    void fizzlesWithoutDrawingWhenTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GruulNodorog());
        harness.setLibrary(player1, List.of(new HatchingPlans()));
        harness.setHand(player1, List.of(new Wildsize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, bear.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new GruulSignet());
        harness.setHand(player1, List.of(new Wildsize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GruulNodorog());
        harness.setLibrary(player1, List.of(new HatchingPlans()));
        harness.setHand(player1, List.of(new Wildsize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, opponentCreature.getId());

        assertThat(opponentCreature.getPowerModifier()).isEqualTo(2);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(2);
        assertThat(opponentCreature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Two Wildsizes stack their boosts and each draw exactly one card")
    void multipleWildsizesStackAndEachDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GruulNodorog());
        harness.setLibrary(player1, List.of(new HatchingPlans(), new GruulSignet(), new HatchingPlans()));
        harness.setHand(player1, List.of(new Wildsize(), new Wildsize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(4);
        assertThat(creature.getToughnessModifier()).isEqualTo(4);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Hatching Plans");
        harness.assertInHand(player1, "Gruul Signet");

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }
}
