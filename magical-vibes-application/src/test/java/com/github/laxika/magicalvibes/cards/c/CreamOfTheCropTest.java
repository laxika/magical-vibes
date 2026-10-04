package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.d.DailyRegimen;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.d.DistantMelody;
import com.github.laxika.magicalvibes.cards.e.Earthbrawn;
import com.github.laxika.magicalvibes.cards.f.Forfend;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        CreamOfTheCrop.class,
        BallyrushBanneret.class,
        ChangelingSentinel.class,
        CoordinatedBarrage.class,
        DailyRegimen.class,
        Disperse.class,
        DistantMelody.class,
        Earthbrawn.class,
        Forfend.class
})
class CreamOfTheCropTest extends BaseCardTest {

    // ===== Look at top X (X = entering creature's power), keep one on top, rest to bottom =====

    @Test
    @DisplayName("Accepting the look puts the chosen card on top and the rest on the bottom")
    void keepsChosenOnTopRestOnBottom() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        // Top of library: Coordinated Barrage, Daily Regimen, Distant Melody.
        harness.setLibrary(player1, List.of(
                new CoordinatedBarrage(), new DailyRegimen(), new DistantMelody()));

        // Ballyrush Banneret (2/1, power 2) enters — look at the top 2 cards.
        harness.castFromHand(player1, new BallyrushBanneret(), "{1}{W}");
        harness.passBothPriorities(); // resolve Ballyrush Banneret (it enters)
        harness.passBothPriorities(); // resolve the MayEffect the trigger queued

        harness.handleMayAbilityChosen(player1, true); // accept the look

        // Look at top 2 [Coordinated Barrage, Daily Regimen]; keep Daily Regimen on top (index 1),
        // Coordinated Barrage to bottom.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).extracting(Card::getName)
                .containsExactly("Daily Regimen", "Distant Melody", "Coordinated Barrage");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== X scales with the entering creature's power =====

    @Test
    @DisplayName("Looks at a number of cards equal to the entering creature's power")
    void looksAtCardsEqualToPower() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        harness.setLibrary(player1, List.of(
                new CoordinatedBarrage(), new DailyRegimen(), new Disperse(), new DistantMelody()));

        // Changeling Sentinel (3/2, power 3) enters — look at the top 3 cards.
        harness.castFromHand(player1, new ChangelingSentinel(), "{3}{W}");
        harness.passBothPriorities(); // resolve Changeling Sentinel
        harness.passBothPriorities(); // resolve the MayEffect

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
    }

    @Test
    @DisplayName("Checks the entering creature's power when the trigger resolves")
    void checksPowerWhenTriggerResolves() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        harness.setLibrary(player1, List.of(
                new CoordinatedBarrage(), new DailyRegimen(), new Disperse(),
                new DistantMelody(), new Forfend()));
        harness.castFromHand(player1, new BallyrushBanneret(), "{1}{W}");
        harness.passBothPriorities();

        var banneret = findPermanent(player1, "Ballyrush Banneret");
        harness.setHand(player1, List.of(new Earthbrawn()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, banneret.getId());
        assertThat(banneret.getEffectivePower()).isEqualTo(5);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(5);
    }

    // ===== Declining leaves the library untouched =====

    @Test
    @DisplayName("Declining the look leaves the library order unchanged")
    void decliningLeavesLibraryUnchanged() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        List<Card> library = List.of(
                new CoordinatedBarrage(), new DailyRegimen(), new DistantMelody());
        harness.setLibrary(player1, library);

        harness.castFromHand(player1, new BallyrushBanneret(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Only triggers for creatures the controller controls =====

    @Test
    @DisplayName("Does not trigger for an opponent's creature entering")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        harness.enterBattlefieldAndReturn(player2, new BallyrushBanneret());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Uses the creature's last known power after it leaves the battlefield")
    void usesLastKnownPowerAfterCreatureLeaves() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        harness.setLibrary(player1, List.of(
                new CoordinatedBarrage(), new DailyRegimen(), new Disperse(),
                new DistantMelody(), new Forfend()));
        harness.castFromHand(player1, new BallyrushBanneret(), "{1}{W}");
        harness.passBothPriorities();
        var banneret = findPermanent(player1, "Ballyrush Banneret");

        harness.setHand(player1, List.of(new Earthbrawn()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, banneret.getId());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, banneret.getId());
        harness.assertNotOnBattlefield(player1, "Ballyrush Banneret");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(5);
    }

    @Test
    @DisplayName("Controller chooses the order of the cards put on the bottom")
    void choosesBottomOrder() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        Card barrage = new CoordinatedBarrage();
        Card regimen = new DailyRegimen();
        Card disperse = new Disperse();
        Card melody = new DistantMelody();
        harness.setLibrary(player1, List.of(barrage, regimen, disperse, melody));
        harness.castFromHand(player1, new ChangelingSentinel(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(barrage, disperse);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(regimen, melody, disperse, barrage);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Looks at all available cards when the library is smaller than the creature's power")
    void looksAtAvailableCardsInShortLibrary() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        Card barrage = new CoordinatedBarrage();
        Card regimen = new DailyRegimen();
        harness.setLibrary(player1, List.of(barrage, regimen));
        harness.castFromHand(player1, new ChangelingSentinel(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(barrage, regimen);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(regimen, barrage);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting with an empty library completes without a card choice")
    void emptyLibraryCompletesWithoutChoice() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new BallyrushBanneret(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
