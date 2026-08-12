package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MudslideTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped non-flying creature stays tapped through the untap step while a flier untaps")
    void nonFlyingCreatureStaysTappedWhileFlierUntaps() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent bears = addTapped(player1, new GrizzlyBears()); // no flying
        Permanent hawk = addTapped(player1, new SuntailHawk());   // flying

        advanceToNextTurn(player2); // roll into player1's untap step

        assertThat(bears.isTapped()).isTrue();
        assertThat(hawk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying {2} untaps the chosen non-flying creature")
    void payingTwoUntapsChosenCreature() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent bears = addTapped(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying {4} untaps two chosen non-flying creatures")
    void payingFourUntapsTwoCreatures() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent bearsA = addTapped(player1, new GrizzlyBears());
        Permanent bearsB = addTapped(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsA.getId(), bearsB.getId()));

        assertThat(bearsA.isTapped()).isFalse();
        assertThat(bearsB.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With only {1} available, no creature can be untapped (cost of {2} not met)")
    void insufficientManaLeavesCreatureTapped() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent bears = addTapped(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A tapped flier is not offered by the upkeep trigger")
    void tappedFlierIsNotOfferedByUpkeepTrigger() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent hawk = addTapped(player1, new SuntailHawk());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(hawk.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("During an opponent's upkeep, that opponent chooses only their own tapped non-fliers")
    void opponentChoosesFromTheirOwnTappedNonFliers() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent ownBears = addTapped(player1, new GrizzlyBears());
        Permanent opponentBears = addTapped(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(opponentBears.getId()).doesNotContain(ownBears.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentBears.getId()));

        assertThat(opponentBears.isTapped()).isFalse();
        assertThat(ownBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing no creatures leaves the non-flying creature tapped")
    void choosingNoneLeavesCreatureTapped() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent bears = addTapped(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.isTapped()).isTrue();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn runs the untap step)
    }
}
