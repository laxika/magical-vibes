package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.b.BrokenWings;
import com.github.laxika.magicalvibes.cards.w.WeatheredRunestone;
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

@CardUsed({RunedCrown.class, RuneOfSustenance.class, BeskirShieldmate.class,
        BrokenWings.class, WeatheredRunestone.class})
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

        resolveAllTriggers();
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
        harness.setLibrary(player1, List.of(new BeskirShieldmate(), rune));

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
        Permanent bears = addCreatureReady(player1, new BeskirShieldmate());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(crown.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(bears.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can fail to find a Rune even when one is available")
    void mayChooseNoRune() {
        RuneOfSustenance rune = new RuneOfSustenance();
        harness.setLibrary(player1, List.of(rune, new BeskirShieldmate()));

        castCrownAndAcceptSearch();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Rune of Sustenance");
        assertThat(gd.playerDecks.get(player1.getId())).contains(rune);
    }

    @Test
    @DisplayName("An accepted search with no Rune leaves the Crown on the battlefield")
    void noRuneAvailable() {
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));
        harness.setGraveyard(player1, List.of());

        castCrownAndAcceptSearch();

        harness.assertOnBattlefield(player1, "Runed Crown");
        harness.assertNotOnBattlefield(player1, "Rune of Sustenance");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Rune stays in hand and does not trigger when the Crown is destroyed in response")
    void runeStaysInHandWhenCrownLeaves() {
        RuneOfSustenance rune = new RuneOfSustenance();
        BeskirShieldmate libraryCard = new BeskirShieldmate();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new RunedCrown(), rune));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent crown = findPermanent(player1, "Runed Crown");

        harness.setHand(player2, List.of(new BrokenWings()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, crown.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(rune.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Runed Crown");
        harness.assertNotOnBattlefield(player1, "Rune of Sustenance");
        harness.assertInHand(player1, "Rune of Sustenance");
        harness.assertNotInGraveyard(player1, "Rune of Sustenance");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Weathered Runestone does not prevent a Rune entering from hand")
    void runeFromHandIgnoresGraveyardEntryRestriction() {
        harness.addToBattlefield(player2, new WeatheredRunestone());
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));
        RuneOfSustenance rune = new RuneOfSustenance();
        harness.setHand(player1, List.of(new RunedCrown(), rune));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(rune.getId()));
        resolveAllTriggers();

        Permanent crown = findPermanent(player1, "Runed Crown");
        Permanent enteredRune = findPermanent(player1, "Rune of Sustenance");
        assertThat(enteredRune.getAttachedTo()).isEqualTo(crown.getId());
        harness.assertNotInHand(player1, "Rune of Sustenance");
        harness.assertNotInGraveyard(player1, "Rune of Sustenance");
        harness.assertInHand(player1, "Beskir Shieldmate");
    }

    @Test
    @DisplayName("Reequipping moves both the power and toughness bonus to the new creature")
    void reequippingMovesBonus() {
        harness.addToBattlefield(player1, new RunedCrown());
        Permanent first = addCreatureReady(player1, new BeskirShieldmate());
        Permanent second = addCreatureReady(player1, new BeskirShieldmate());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    private Permanent castCrownAndAcceptSearch() {
        return castCrownAndAcceptSearch(true);
    }

    private Permanent castCrownAndAcceptSearch(boolean accept) {
        harness.castFromHand(player1, new RunedCrown(), "{3}");

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, accept);
        return findPermanent(player1, "Runed Crown");
    }
}
