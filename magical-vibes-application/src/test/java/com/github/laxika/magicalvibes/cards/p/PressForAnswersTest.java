package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({PressForAnswers.class, GrizzlyBears.class, Forest.class})
class PressForAnswersTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target creature, locks its next untap step, and creates a Clue")
    void tapsLocksAndInvestigates() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressForAnswers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PressForAnswers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lockExpiresAfterTargetsControllersNextUntapStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressForAnswers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.performUntapStep(player1);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedCreatureIsStillLockedAndInvestigates() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new PressForAnswers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    void doesNotInvestigateWhenOnlyTargetLeavesBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressForAnswers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, bears.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertInGraveyard(player1, "Press for Answers");
    }

    @Test
    void clueCanBeSacrificedForTwoManaToDrawOneCard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new PressForAnswers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, bears.getId());

        Permanent clue = findPermanents(player1, "Clue").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.activateAbility(player1, clueIndex, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }
}
