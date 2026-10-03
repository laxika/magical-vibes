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
        resolveAllTriggers();
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
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotOnBattlefield(player1, "Calim, Djinn Emperor");
        harness.assertInGraveyard(player1, "Calim, Djinn Emperor");
        assertThat(gd.exiledCards)
                .extracting(exiled -> exiled.card().getId())
                .containsExactlyInAnyOrder(otherOne.getId(), otherTwo.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Calim, Djinn Emperor")
                        && permanent.isTapped());
        assertThat(gd.exiledCards)
                .extracting(exiled -> exiled.card().getId())
                .containsExactlyInAnyOrder(otherOne.getId(), otherTwo.getId());
        harness.assertNotInGraveyard(player1, "Calim, Djinn Emperor");
    }

    @Test
    @DisplayName("The discarded source cannot count as one of the two other Calims")
    void breathCannotExileSourceToMakeUpTwoCopies() {
        Card source = new CalimDjinnEmperor();
        Card other = new CalimDjinnEmperor();
        harness.setHand(player1, List.of(source));
        harness.setGraveyard(player1, List.of(other));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, source);
        assertThat(gd.exiledCards).isEmpty();
        harness.assertNotOnBattlefield(player1, "Calim, Djinn Emperor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An illegal sole target stops the draw but not the independent discard trigger")
    void illegalTargetStopsBreathButStillConjures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card source = new CalimDjinnEmperor();
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player1, List.of(source));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getName())
                .isEqualTo("Calim, Djinn Emperor");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
    }

    @Test
    @DisplayName("Discarding Calim into an empty library conjures a new copy before the draw")
    @CardUsed(CalimDjinnEmperor.class)
    void breathDrawsConjuredCopyFromInitiallyEmptyLibrary() {
        Card source = new CalimDjinnEmperor();
        harness.setHand(player1, List.of(source));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertNotInHand(player1, "Calim, Djinn Emperor");
        harness.assertInGraveyard(player1, "Calim, Djinn Emperor");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        Card conjured = gd.playerDecks.get(player1.getId()).getFirst();
        assertThat(conjured.getName()).isEqualTo("Calim, Djinn Emperor");
        assertThat(conjured.getId()).isNotEqualTo(source.getId());
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(conjured);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
    }
}
