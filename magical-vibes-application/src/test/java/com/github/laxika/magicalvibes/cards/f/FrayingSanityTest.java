package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.StripedRiverwinder;
import com.github.laxika.magicalvibes.cards.w.WanderInDeath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrayingSanity.class, StripedRiverwinder.class, WanderInDeath.class})
class FrayingSanityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Fraying Sanity attaches it to the target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new FrayingSanity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fraying Sanity")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    @Test
    @DisplayName("Enchanted player mills a number of cards equal to cards put into their graveyard this turn")
    void millsEqualToCardsPutIntoGraveyardThisTurn() {
        attachFrayingSanityTo(player2);
        seedCardsPutIntoGraveyardThisTurn(player2, 3);

        int deckBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToEndStep(player2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve mill trigger

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 3);
    }

    @Test
    @DisplayName("Enchanted player mills nothing when no cards were put into their graveyard this turn")
    void millsNothingWhenNoCardsThisTurn() {
        attachFrayingSanityTo(player2);

        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Trigger fires at each end step — including the Aura controller's turn")
    void firesDuringAuraControllerEndStep() {
        attachFrayingSanityTo(player2);
        seedCardsPutIntoGraveyardThisTurn(player2, 2);

        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        // player1 controls the Curse; the trigger still fires at their end step and mills player2.
        // (Assert on the graveyard, not the library: after the mill resolves, priority passing rolls
        // on into player2's next turn where their draw step would also shrink their library.)
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 2);
    }

    @Test
    @DisplayName("No mill after Fraying Sanity leaves the battlefield")
    void noMillAfterRemoval() {
        Permanent aura = attachFrayingSanityTo(player2);
        seedCardsPutIntoGraveyardThisTurn(player2, 3);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Multiple Curses recalculate the graveyard count as each trigger resolves")
    void multipleCursesCompoundMilling() {
        attachFrayingSanityTo(player2);
        attachFrayingSanityTo(player2);
        seedCardsPutIntoGraveyardThisTurn(player2, 2);
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToEndStep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 6);
    }

    @Test
    @DisplayName("A queued trigger still mills after its Aura leaves the battlefield")
    void queuedTriggerSurvivesAuraRemoval() {
        Permanent aura = attachFrayingSanityTo(player2);
        seedCardsPutIntoGraveyardThisTurn(player2, 2);
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToEndStep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 2);
    }

    @Test
    @DisplayName("Milling more cards than remain in the library mills all remaining cards")
    void millsOnlyAvailableCards() {
        attachFrayingSanityTo(player2);
        seedCardsPutIntoGraveyardThisTurn(player2, 3);
        harness.setLibrary(player2, List.of(new FrayingSanity()));
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 1);
    }

    @Test
    @DisplayName("Counts each graveyard entry when the same card is cycled twice in one turn")
    void countsRepeatedGraveyardEntries() {
        attachFrayingSanityTo(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        StripedRiverwinder riverwinder = new StripedRiverwinder();
        harness.setHand(player1, List.of(riverwinder, new WanderInDeath()));
        harness.setLibrary(player1, List.of(new FrayingSanity(), new FrayingSanity(),
                new FrayingSanity(), new FrayingSanity(), new FrayingSanity(), new FrayingSanity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(riverwinder.getId()));
        harness.passBothPriorities();
        int riverwinderIndex = gd.playerHands.get(player1.getId()).indexOf(riverwinder);
        assertThat(riverwinderIndex).isNotNegative();
        harness.activateHandAbility(player1, riverwinderIndex, null);
        harness.passBothPriorities();
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        // Two cycling discards and the resolved return spell each entered the graveyard this turn.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 3);
    }

    private Permanent attachFrayingSanityTo(Player enchantedPlayer) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FrayingSanity());
        aura.setAttachedTo(enchantedPlayer.getId());
        return aura;
    }

    private void seedCardsPutIntoGraveyardThisTurn(Player player, int count) {
        HashSet<UUID> ids = new HashSet<>();
        for (int i = 0; i < count; i++) {
            ids.add(UUID.randomUUID());
        }
        gd.cardsPutIntoGraveyardFromAnywhereThisTurn.put(player.getId(), ids);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
