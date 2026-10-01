package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.c.CennsTactician;
import com.github.laxika.magicalvibes.cards.c.ChangelingSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Reveillark.class, BallyrushBanneret.class, CennsTactician.class, ChangelingSentinel.class})
class ReveillarkTest extends BaseCardTest {

    private void killReveillark(Permanent reveillark) {
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, reveillark));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance the trigger to its target-choice interaction
    }

    @Test
    @DisplayName("LTB can target two small creatures from its controller's graveyard")
    void returnsTwoSmallCreatures() {
        CennsTactician first = new CennsTactician();
        BallyrushBanneret second = new BallyrushBanneret();
        harness.setGraveyard(player1, List.of(first, second));
        Permanent reveillark = harness.addToBattlefieldAndReturn(player1, new Reveillark());

        killReveillark(reveillark);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.minCount()).isZero();
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cenn's Tactician")).isEqualTo(1);
        assertThat(countPermanents(player1, "Ballyrush Banneret")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Cenn's Tactician");
        harness.assertNotInGraveyard(player1, "Ballyrush Banneret");
    }

    @Test
    @DisplayName("Creatures with power 3 or more are not eligible to return")
    void powerThreeCreatureNotReturned() {
        ChangelingSentinel tooLarge = new ChangelingSentinel();
        BallyrushBanneret eligible = new BallyrushBanneret();
        harness.setGraveyard(player1, List.of(tooLarge, eligible));
        Permanent reveillark = harness.addToBattlefieldAndReturn(player1, new Reveillark());

        killReveillark(reveillark);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Ballyrush Banneret")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Ballyrush Banneret");
        harness.assertInGraveyard(player1, "Changeling Sentinel");
    }

    @Test
    @DisplayName("Reveillark itself (power 4) is not returned by its own trigger")
    void doesNotReturnItself() {
        harness.setGraveyard(player1, List.of());
        Permanent reveillark = harness.addToBattlefieldAndReturn(player1, new Reveillark());

        killReveillark(reveillark);

        assertThat(countPermanents(player1, "Reveillark")).isZero();
        harness.assertInGraveyard(player1, "Reveillark");
    }

    @Test
    @DisplayName("With more than two eligible creatures, controller chooses exactly two")
    void choosesTwoOfThree() {
        CennsTactician first = new CennsTactician();
        BallyrushBanneret middle = new BallyrushBanneret();
        CennsTactician third = new CennsTactician();
        harness.setGraveyard(player1, List.of(first, middle, third));
        Permanent reveillark = harness.addToBattlefieldAndReturn(player1, new Reveillark());

        killReveillark(reveillark);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.minCount()).isZero();
        assertThat(choice.validCardIds()).containsExactly(first.getId(), middle.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), third.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cenn's Tactician")).isEqualTo(2);
        assertThat(countPermanents(player1, "Ballyrush Banneret")).isZero();
        harness.assertInGraveyard(player1, "Ballyrush Banneret");
    }

    @Test
    @DisplayName("LTB may target no creature")
    void mayChooseNoTargets() {
        CennsTactician eligible = new CennsTactician();
        harness.setGraveyard(player1, List.of(eligible));
        Permanent reveillark = harness.addToBattlefieldAndReturn(player1, new Reveillark());

        killReveillark(reveillark);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.minCount()).isZero();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Cenn's Tactician");
        harness.assertNotOnBattlefield(player1, "Cenn's Tactician");
    }

    @Test
    @DisplayName("Evoke: sacrificed on entry, then returns small creatures from graveyard")
    void evokeSacrificeThenReturn() {
        BallyrushBanneret eligible = new BallyrushBanneret();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setHand(player1, List.of(new Reveillark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Reveillark")).isZero();
        assertThat(countPermanents(player1, "Ballyrush Banneret")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Reveillark");
    }
}
