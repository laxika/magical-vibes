package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FrayingSanityTest extends BaseCardTest {

    // ===== Casting and resolving =====

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

    // ===== End step mill trigger =====

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

    // ===== Helpers =====

    private Permanent attachFrayingSanityTo(Player enchantedPlayer) {
        Permanent aura = new Permanent(new FrayingSanity());
        aura.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
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
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to END_STEP, trigger queued
    }
}
