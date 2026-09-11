package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({BoughsideWanderers.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class})
class BoughsideWanderersTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability may reveal a permanent from the top four")
    void mayRevealPermanentFromTopFour() {
        Card permanent = new GrizzlyBears();
        Card instant = new Shock();
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(permanent, instant, forest, island));
        harness.setHand(player1, List.of(new BoughsideWanderers()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).contains(permanent.getId(), forest.getId(), island.getId())
                .doesNotContain(instant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(permanent);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(instant, forest, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Landfall gives Boughside Wanderers +2/+2 until end of turn")
    void landfallBoostsUntilEndOfTurn() {
        Permanent wanderers = harness.addToBattlefieldAndReturn(player1, new BoughsideWanderers());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(wanderers.getEffectivePower()).isEqualTo(6);
        assertThat(wanderers.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wanderers.getEffectivePower()).isEqualTo(4);
        assertThat(wanderers.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Boughside Wanderers")
    void opponentLandDoesNotTrigger() {
        Permanent wanderers = harness.addToBattlefieldAndReturn(player1, new BoughsideWanderers());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(wanderers.getEffectivePower()).isEqualTo(4);
        assertThat(wanderers.getEffectiveToughness()).isEqualTo(4);
    }
}
