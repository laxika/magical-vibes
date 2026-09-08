package com.github.laxika.magicalvibes.cards.r;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuneforgeChampion.class, RuneOfSustenance.class, GrizzlyBears.class})
class RuneforgeChampionTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability returns a Rune from the graveyard")
    void searchesGraveyardForRune() {
        RuneOfSustenance rune = new RuneOfSustenance();
        harness.setGraveyard(player1, List.of(rune));
        castChampion();

        resolveEnterTheBattlefieldTrigger(true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(rune.getId()));

        harness.assertInHand(player1, "Rune of Sustenance");
        harness.assertNotInGraveyard(player1, "Rune of Sustenance");
    }

    @Test
    @DisplayName("The enter-the-battlefield ability searches the library for a Rune")
    void searchesLibraryForRune() {
        Card rune = new RuneOfSustenance();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), rune));
        castChampion();

        resolveEnterTheBattlefieldTrigger(true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.pool()).containsExactly(rune);

        harness.handleMultipleCardsChosen(player1, List.of(rune.getId()));
        harness.assertInHand(player1, "Rune of Sustenance");
    }

    @Test
    @DisplayName("Declining the enter-the-battlefield ability does nothing")
    void declinesSearch() {
        RuneOfSustenance rune = new RuneOfSustenance();
        harness.setGraveyard(player1, List.of(rune));
        castChampion();

        resolveEnterTheBattlefieldTrigger(false);

        harness.assertInGraveyard(player1, "Rune of Sustenance");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Rune spell can be cast for one generic mana")
    void castsRuneForOneMana() {
        harness.addToBattlefield(player1, new RuneforgeChampion());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuneOfSustenance()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A non-Rune spell cannot use the Rune alternative cost")
    void doesNotReduceNonRuneSpellCost() {
        harness.addToBattlefield(player1, new RuneforgeChampion());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castChampion() {
        harness.setHand(player1, List.of(new RuneforgeChampion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTheBattlefieldTrigger(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
