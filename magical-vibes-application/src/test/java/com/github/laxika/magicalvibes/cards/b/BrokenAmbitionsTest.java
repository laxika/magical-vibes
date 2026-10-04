package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OakgnarlWarrior;
import com.github.laxika.magicalvibes.cards.s.SpellbreakerBehemoth;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrokenAmbitions.class, Forest.class, WoodlandChangeling.class, OakgnarlWarrior.class,
        SpellbreakerBehemoth.class})
class BrokenAmbitionsTest extends BaseCardTest {

    private WoodlandChangeling prepareCounterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        WoodlandChangeling target = new WoodlandChangeling();

        harness.setHand(player2, List.of(new BrokenAmbitions()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        return target;
    }

    // Player2 (Broken Ambitions' caster) wins the clash: their revealed top card has a strictly
    // greater mana value than player1's.
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new WoodlandChangeling(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Countering and winning the clash mills the countered spell's controller four cards")
    void wonClashMillsCounteredSpellController() {
        WoodlandChangeling target = prepareCounterTarget();
        stackClashWinForCaster();
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, target.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();
        keepClashCardsOnTop();

        // Spell was countered (player1 could not pay {1}).
        harness.assertNotOnBattlefield(player1, "Woodland Changeling");
        harness.assertInGraveyard(player1, "Woodland Changeling");
        // The winning clash mills four cards from the targeted spell controller.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 4);
    }

    @Test
    @DisplayName("Losing the clash counters the spell but mills nothing")
    void lostClashMillsNothing() {
        WoodlandChangeling target = prepareCounterTarget();
        // Player2 loses the clash: player1 reveals the higher mana value.
        harness.setLibrary(player1, List.of(
                new WoodlandChangeling(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, target.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Woodland Changeling");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore); // no mill
    }

    @Test
    @DisplayName("A tied clash counters the spell but mills nothing")
    void tiedClashMillsNothing() {
        WoodlandChangeling target = prepareCounterTarget();
        harness.setLibrary(player1, List.of(
                new WoodlandChangeling(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(
                new WoodlandChangeling(), new Forest(), new Forest()));
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, target.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Woodland Changeling");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }

    @Test
    @DisplayName("Paying X keeps the spell but a won clash still mills its controller four cards")
    void paidSpellStillMilledOnWonClash() {
        WoodlandChangeling target = prepareCounterTarget();
        stackClashWinForCaster();
        harness.addMana(player1, ManaColor.GREEN, 1); // spare mana to pay {1}
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, target.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();
        keepClashCardsOnTop();

        // Payment is decided before the clash reveals any cards or mills.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        keepClashCardsOnTop(); // pay {1}
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 4);
        harness.passBothPriorities();
        keepClashCardsOnTop(); // resolve Woodland Changeling

        harness.assertOnBattlefield(player1, "Woodland Changeling");
    }

    @Test
    @DisplayName("An uncounterable spell still clashes and mills its controller on a win")
    void uncounterableSpellStillClashesAndMills() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());

        OakgnarlWarrior warrior = new OakgnarlWarrior();
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        harness.setHand(player2, List.of(new BrokenAmbitions()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1
        harness.setLibrary(player2, List.of(new WoodlandChangeling(), new Forest(), new Forest()));

        harness.castFromHand(player1, warrior, "{5}{G}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, warrior.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();
        keepClashCardsOnTop();
        harness.passBothPriorities();
        keepClashCardsOnTop();

        harness.assertOnBattlefield(player1, "Oakgnarl Warrior");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 4);
    }

    @Test
    @DisplayName("Declining payment counters the spell and a won clash still mills four")
    void declinedPaymentStillMillsOnWin() {
        WoodlandChangeling target = prepareCounterTarget();
        stackClashWinForCaster();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, target.getId());
        harness.passBothPriorities();
        keepClashCardsOnTop();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Woodland Changeling");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Paying zero preserves the spell and does not prevent clash milling")
    void zeroXCanBePaidAndStillMills() {
        WoodlandChangeling target = prepareCounterTarget();
        stackClashWinForCaster();
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        keepClashCardsOnTop();
        harness.handleMayAbilityChosen(player1, true);
        keepClashCardsOnTop();
        harness.passBothPriorities();
        keepClashCardsOnTop();

        harness.assertOnBattlefield(player1, "Woodland Changeling");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A won clash mills only the cards remaining in a short library")
    void millsShortLibraryWithoutDrawing() {
        WoodlandChangeling target = prepareCounterTarget();
        stackClashWinForCaster();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, target.getId());
        harness.passBothPriorities();
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Woodland Changeling");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The caster cannot win a clash without revealing a card")
    void emptyCasterLibraryCannotWin() {
        WoodlandChangeling target = prepareCounterTarget();
        stackClashWinForCaster();
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, target.getId());
        harness.passBothPriorities();
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Woodland Changeling");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Clashing players must be offered their revealed card placement choices")
    void clashOffersLibraryPlacementChoices() {
        WoodlandChangeling target = prepareCounterTarget();
        harness.setLibrary(player1, List.of(new WoodlandChangeling(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new WoodlandChangeling()));
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Woodland Changeling");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
    private void keepClashCardsOnTop() {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry scry) {
            gs.handleInteractionAnswer(gd,
                    scry.playerId().equals(player1.getId()) ? player1 : player2,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }
    }
}
