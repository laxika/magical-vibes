package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RunedCrownTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a Rune from the graveyard onto the battlefield attached to Runed Crown")
    void searchesGraveyardForRune() {
        RuneOfSustenance rune = new RuneOfSustenance();
        harness.setGraveyard(player1, List.of(rune));

        Permanent crown = castCrownAndAcceptSearch();
        harness.handleMultipleCardsChosen(player1, List.of(rune.getId()));

        Permanent enteredRune = findPermanent(player1, "Rune of Sustenance");
        assertThat(enteredRune.getAttachedTo()).isEqualTo(crown.getId());
        harness.assertNotInGraveyard(player1, "Rune of Sustenance");
    }

    @Test
    @DisplayName("Puts a Rune from the hand onto the battlefield attached to Runed Crown")
    void searchesHandForRune() {
        RuneOfSustenance rune = new RuneOfSustenance();
        harness.setHand(player1, List.of(new RunedCrown(), rune));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(rune.getId()));

        Permanent crown = findPermanent(player1, "Runed Crown");
        Permanent enteredRune = findPermanent(player1, "Rune of Sustenance");
        assertThat(enteredRune.getAttachedTo()).isEqualTo(crown.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(rune);
    }

    @Test
    @DisplayName("Searches the library for a Rune and attaches it to Runed Crown")
    void searchesLibraryForRune() {
        Card rune = new RuneOfSustenance();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), rune));

        Permanent crown = castCrownAndAcceptSearch();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice search =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.pool()).containsExactly(rune);

        harness.handleMultipleCardsChosen(player1, List.of(rune.getId()));

        Permanent enteredRune = findPermanent(player1, "Rune of Sustenance");
        assertThat(enteredRune.getAttachedTo()).isEqualTo(crown.getId());
    }

    @Test
    @DisplayName("May decline the Rune search")
    void declinesSearch() {
        RuneOfSustenance rune = new RuneOfSustenance();
        harness.setGraveyard(player1, List.of(rune));

        castCrownAndAcceptSearch(false);

        harness.assertInGraveyard(player1, "Rune of Sustenance");
        assertThat(findPermanents(player1, "Rune of Sustenance")).isEmpty();
    }

    @Test
    @DisplayName("Gives the equipped creature +1/+1")
    void boostsEquippedCreature() {
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new RunedCrown());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(crown.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(bears.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    private Permanent castCrownAndAcceptSearch() {
        return castCrownAndAcceptSearch(true);
    }

    private Permanent castCrownAndAcceptSearch(boolean accept) {
        harness.setHand(player1, List.of(new RunedCrown()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
        return findPermanent(player1, "Runed Crown");
    }
}
