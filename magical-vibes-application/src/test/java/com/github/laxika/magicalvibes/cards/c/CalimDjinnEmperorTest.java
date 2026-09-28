package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalimDjinnEmperor.class, GrizzlyBears.class})
class CalimDjinnEmperorTest extends BaseCardTest {

    @Test
    @DisplayName("Calim's Breath taps a nonland permanent, draws, and conjures Calim seventh from the top")
    void breathTapsDrawsAndConjuresCalim() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CalimDjinnEmperor()));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Calim, Djinn Emperor");
        List<Card> library = gd.playerDecks.get(player1.getId());
        // The discard trigger resolves before the activated ability, so the subsequent draw shifts
        // the conjured seventh-from-top card from index 6 to index 5.
        assertThat(library.get(5).getName())
                .withFailMessage("Library order: %s", library.stream().map(Card::getName).toList())
                .isEqualTo("Calim, Djinn Emperor");
    }

    @Test
    @DisplayName("Calim's Breath exiles two other Calims and returns the discarded source tapped")
    void breathExilesOtherCalimsAndReturnsSource() {
        Card otherOne = new CalimDjinnEmperor();
        Card otherTwo = new CalimDjinnEmperor();
        harness.setGraveyard(player1, List.of(otherOne, otherTwo));
        harness.setHand(player1, List.of(new CalimDjinnEmperor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Calim, Djinn Emperor")
                        && permanent.isTapped());
        assertThat(gd.exiledCards)
                .extracting(exiled -> exiled.card().getId())
                .containsExactlyInAnyOrder(otherOne.getId(), otherTwo.getId());
        harness.assertNotInGraveyard(player1, "Calim, Djinn Emperor");
    }
}
