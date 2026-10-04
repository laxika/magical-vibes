package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HouseCartographer.class, Forest.class, GrizzlyBears.class, RelentlessAssault.class})
class HouseCartographerTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped House Cartographer puts the first revealed land into hand and the rest on the bottom")
    void tappedCartographerFindsFirstLand() {
        Permanent cartographer = harness.addToBattlefieldAndReturn(player1, new HouseCartographer());
        Card firstNonland = new GrizzlyBears();
        Forest land = new Forest();
        Card lastNonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstNonland, land, lastNonland));
        cartographer.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNonland, lastNonland);
    }

    @Test
    @DisplayName("A tapped House Cartographer bottoms all revealed cards when no land is found")
    void noLandFoundBottomsEverything() {
        Permanent cartographer = harness.addToBattlefieldAndReturn(player1, new HouseCartographer());
        Card firstNonland = new GrizzlyBears();
        Card lastNonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstNonland, lastNonland));
        cartographer.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(firstNonland, lastNonland);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNonland, lastNonland);
    }

    @Test
    @DisplayName("An untapped House Cartographer does not trigger Survival")
    void untappedCartographerDoesNotTrigger() {
        Forest land = new Forest();
        harness.addToBattlefield(player1, new HouseCartographer());
        harness.setLibrary(player1, List.of(land));

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void untappingBeforeResolutionStopsTheAbility() {
        Permanent cartographer = harness.addToBattlefieldAndReturn(player1, new HouseCartographer());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        cartographer.tap();

        advanceToPostcombatMain();
        assertThat(gd.stack).hasSize(1);
        cartographer.untap();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void unrevealedCardsStayAboveTheBottomedCardsInTheirOriginalOrder() {
        Permanent cartographer = harness.addToBattlefieldAndReturn(player1, new HouseCartographer());
        Card firstRevealed = new HouseCartographer();
        Card secondRevealed = new HouseCartographer();
        Forest foundLand = new Forest();
        Card firstUnrevealed = new HouseCartographer();
        Forest secondUnrevealed = new Forest();
        harness.setLibrary(player1, List.of(firstRevealed, secondRevealed, foundLand,
                firstUnrevealed, secondUnrevealed));
        cartographer.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(foundLand).doesNotContain(secondUnrevealed);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .startsWith(firstUnrevealed, secondUnrevealed);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(firstRevealed, secondRevealed);
    }

    @Test
    void doesNotTriggerAgainInThirdMainPhase() {
        Permanent cartographer = harness.addToBattlefieldAndReturn(player1, new HouseCartographer());
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        cartographer.tap();
        advanceToPostcombatMain();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(firstLand);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondLand);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(secondLand);
    }

    @Test
    void emptyLibraryRevealsNothing() {
        Permanent cartographer = harness.addToBattlefieldAndReturn(player1, new HouseCartographer());
        harness.setLibrary(player1, List.of());
        int handSize = gd.playerHands.get(player1.getId()).size();
        cartographer.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerInOpponentsSecondMainPhase() {
        Permanent cartographer = harness.addToBattlefieldAndReturn(player1, new HouseCartographer());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        cartographer.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
    }
}
