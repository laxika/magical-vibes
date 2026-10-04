package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefiantStrike.class, GrizzlyBears.class, FountainOfYouth.class})
class DefiantStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +1/+0 to target creature and draws a card")
    void boostsAndDraws() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DefiantStrike()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();

        List<Card> hand = harness.getGameData().playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DefiantStrike()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DefiantStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost an opponent's creature while only the caster draws")
    void boostsOpponentsCreatureAndCasterDraws() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears drawnCard = new GrizzlyBears();
        GrizzlyBears opponentLibraryCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new DefiantStrike()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
        harness.assertInGraveyard(player1, "Defiant Strike");
    }

    @Test
    @DisplayName("Does not draw when its only target leaves the battlefield before resolution")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new DefiantStrike()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bear));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Defiant Strike");
        assertThat(gd.stack).isEmpty();
    }
}
