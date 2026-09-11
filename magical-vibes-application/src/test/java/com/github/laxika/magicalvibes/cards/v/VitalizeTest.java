package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vitalize.class, GrizzlyBears.class, Island.class})
class VitalizeTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all tapped creatures you control")
    void untapsAllTappedCreaturesYouControl() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear1.tap();
        Permanent bear2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear2.tap();

        harness.castFromHand(player1, new Vitalize(), "{G}");
        harness.passBothPriorities();

        assertThat(bear1.isTapped()).isFalse();
        assertThat(bear2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps every creature without prompting for a choice")
    void untapsWithoutPromptingForAChoice() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).tap();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).tap();

        harness.castFromHand(player1, new Vitalize(), "{G}");
        harness.passBothPriorities();

        // "Untap all creatures you control" has no chosenCount, so it must not take the
        // "untap up to N" branch that Rewind and Unwind share with it.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentBear.tap();

        harness.castFromHand(player1, new Vitalize(), "{G}");
        harness.passBothPriorities();

        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap non-creature permanents you control")
    void doesNotUntapNonCreaturePermanents() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        harness.castFromHand(player1, new Vitalize(), "{G}");
        harness.passBothPriorities();

        assertThat(island.isTapped()).isTrue();
    }
}
