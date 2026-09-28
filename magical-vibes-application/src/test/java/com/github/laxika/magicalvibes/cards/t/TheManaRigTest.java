package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
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

@CardUsed({TheManaRig.class, WoollyThoctar.class, GrizzlyBears.class})
class TheManaRigTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a multicolored spell creates a tapped Powerstone")
    void multicoloredSpellCreatesTappedPowerstone() {
        harness.addToBattlefield(player1, new TheManaRig());
        harness.setHand(player1, List.of(new WoollyThoctar()));
        addWoollyThoctarMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability puts up to two of the top X cards into hand")
    void activatedAbilityChoosesUpToTwoCards() {
        Card first = new GrizzlyBears();
        Card second = new WoollyThoctar();
        Card third = new GrizzlyBears();
        Card untouched = new WoollyThoctar();
        harness.setLibrary(player1, List.of(first, second, third, untouched));
        harness.addToBattlefield(player1, new TheManaRig());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(first, second, third);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(third, untouched);
        assertThat(findPermanent(player1, "The Mana Rig").isTapped()).isTrue();
    }

    private void addWoollyThoctarMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
