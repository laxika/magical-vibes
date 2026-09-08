package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantKoi.class, GrizzlyBears.class, Island.class})
class GiantKoiTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend makes Giant Koi unblockable this turn")
    void waterbendMakesGiantKoiUnblockable() {
        Permanent koi = harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(koi.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(koi.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The Waterbend unblockable effect wears off at end of turn")
    void waterbendUnblockableWearsOffAtEndOfTurn() {
        Permanent koi = harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(koi.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(koi.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Waterbend cannot be paid without three available payments")
    void waterbendRequiresThreePayments() {
        Permanent koi = harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(koi.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(koi.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Islandcycling searches for an Island and discards Giant Koi")
    void islandcyclingSearchesForIsland() {
        Card koi = new GiantKoi();
        Island island = new Island();
        harness.setHand(player1, List.of(koi));
        harness.setLibrary(player1, List.of(island, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(koi);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(card -> card instanceof Island);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(island);
    }
}
