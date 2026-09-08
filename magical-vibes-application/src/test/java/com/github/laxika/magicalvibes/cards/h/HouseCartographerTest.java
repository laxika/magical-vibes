package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HouseCartographer.class, Forest.class, GrizzlyBears.class})
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

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
