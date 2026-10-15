package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConfoundingRiddle.class, Shock.class})
class ConfoundingRiddleTest extends BaseCardTest {

    @Test
    void lookModePutsOneCardIntoHandAndTheRestIntoGraveyard() {
        Card first = new Shock();
        Card chosen = new Shock();
        Card third = new Shock();
        Card fourth = new Shock();
        harness.setLibrary(player1, List.of(first, chosen, third, fourth));
        harness.setHand(player1, List.of(new ConfoundingRiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void counterModeCountersTargetSpellUnlessItsControllerPaysFour() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new ConfoundingRiddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{1}, shock.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lookModeCannotDeclineToPutACardIntoHand() {
        harness.setLibrary(player1, List.of(new ConfoundingRiddle(), new ConfoundingRiddle()));
        harness.setHand(player1, List.of(new ConfoundingRiddle()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lookModeUsesAllCardsInAShortLibrary() {
        Card chosen = new ConfoundingRiddle();
        Card other = new ConfoundingRiddle();
        harness.setLibrary(player1, List.of(chosen, other));
        harness.setHand(player1, List.of(new ConfoundingRiddle()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void lookModeWithEmptyLibraryDoesNotDrawOrRequireAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ConfoundingRiddle()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Confounding Riddle");
    }

    @Test
    void payingFourPreservesTheTargetSpellAndSpendsTheControllersMana() {
        ConfoundingRiddle target = new ConfoundingRiddle();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLibrary(player1, List.of());
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player2, List.of(new ConfoundingRiddle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{1}, target.getId(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningToPayFourCountersTheSpellWithoutSpendingMana() {
        ConfoundingRiddle target = new ConfoundingRiddle();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player2, List.of(new ConfoundingRiddle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{1}, target.getId(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }
}
