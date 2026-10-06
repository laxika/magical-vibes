package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResistanceSquad.class, EliteVanguard.class, GrizzlyBears.class})
class ResistanceSquadTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card when you control another Human")
    void etbDrawsWithAnotherHuman() {
        harness.addToBattlefield(player1, new EliteVanguard());
        int handBefore = castResistanceSquad();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("ETB does not draw when Resistance Squad is your only Human")
    void etbDoesNotDrawWithoutAnotherHuman() {
        int handBefore = castResistanceSquad();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("ETB does not draw for an opponent's Human")
    void etbIgnoresOpponentsHuman() {
        harness.addToBattlefield(player2, new EliteVanguard());
        int handBefore = castResistanceSquad();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A non-Human creature does not enable the draw trigger")
    void etbIgnoresNonHumanCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        int handBefore = castResistanceSquad();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A Human entering after Resistance Squad cannot create a missed trigger")
    void humanArrivingAfterEntryDoesNotEnableDraw() {
        int handBefore = castResistanceSquad();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.enterBattlefieldAndReturn(player1, new EliteVanguard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The draw requires another Human when the trigger resolves")
    void noDrawWhenOtherHumanLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new ResistanceSquad());
        var otherHuman = findPermanent(player1, "Resistance Squad");
        int handBefore = castResistanceSquad();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, otherHuman));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The draw still resolves when Resistance Squad leaves but another Human remains")
    void drawsWhenSourceLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new ResistanceSquad());
        int handBefore = castResistanceSquad();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        var source = findPermanents(player1, "Resistance Squad").get(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Multiple other Humans cause only one card to be drawn")
    void drawsOnlyOneCardWithMultipleHumans() {
        harness.addToBattlefield(player1, new ResistanceSquad());
        harness.addToBattlefield(player1, new ResistanceSquad());
        int handBefore = castResistanceSquad();
        harness.setLibrary(player1, List.of(new ResistanceSquad(), new ResistanceSquad()));

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private int castResistanceSquad() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new ResistanceSquad(), "{2}{W}");
        return gd.playerHands.get(player1.getId()).size();
    }
}
