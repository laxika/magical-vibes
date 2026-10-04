package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DecisionParalysis.class, GrizzlyBears.class, Forest.class})
class DecisionParalysisTest extends BaseCardTest {

    @Test
    @DisplayName("Taps both target creatures and locks their next untap step")
    void tapsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecisionParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("May tap a single creature (up to two)")
    void tapsSingleCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecisionParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DecisionParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canResolveWithNoTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecisionParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(bears.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Decision Paralysis");
    }

    @Test
    void alreadyTappedCreatureSkipsOnlyItsControllersNextUntap() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new DecisionParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.performUntapStep(player1);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void canTargetCreaturesControlledByDifferentPlayers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecisionParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, List.of(own.getId(), opposing.getId()));

        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isFalse();
    }

    @Test
    void resolvesForRemainingTargetWhenOneLeavesBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecisionParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());

        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(second.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void cannotChooseMoreThanTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecisionParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseSameCreatureTwice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecisionParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void overlappingSpellsDoNotPreventTwoUntapSteps() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecisionParalysis(), new DecisionParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }
}
