package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenFamiliar.class, GiantCockroach.class})
class RavenFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts one of the top three cards into hand and bottoms the rest in order")
    void etbPicksOneAndBottomOrdersTheRest() {
        Card top = new GiantCockroach();
        Card second = new GiantCockroach();
        Card third = new GiantCockroach();
        Card untouched = new GiantCockroach();
        harness.setLibrary(player1, List.of(top, second, third, untouched));
        harness.castFromHand(player1, new RavenFamiliar(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(top.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        PendingInteraction.LibraryReorder reorder = gd.interaction
                .activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(top, third);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top, third, untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, top);
    }

    @Test
    @DisplayName("ETB takes the only available card when the library has fewer than three")
    void etbUsesAvailableCardsOnly() {
        Card only = new GiantCockroach();
        harness.setLibrary(player1, List.of(only));
        harness.castFromHand(player1, new RavenFamiliar(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining echo sacrifices Raven Familiar at its next upkeep")
    void decliningEchoSacrificesRavenFamiliarAtNextUpkeep() {
        castAndResolveRavenFamiliar();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Raven Familiar");
        harness.assertInGraveyard(player1, "Raven Familiar");
    }

    @Test
    @DisplayName("Paying echo keeps Raven Familiar and echo does not trigger again")
    void payingEchoKeepsRavenFamiliarAndEchoDoesNotTriggerAgain() {
        castAndResolveRavenFamiliar();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Raven Familiar");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Raven Familiar");
    }

    @Test
    @DisplayName("Echo does not trigger during an opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveRavenFamiliar();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Raven Familiar");
    }

    private void castAndResolveRavenFamiliar() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new RavenFamiliar(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Raven Familiar");
    }
}
