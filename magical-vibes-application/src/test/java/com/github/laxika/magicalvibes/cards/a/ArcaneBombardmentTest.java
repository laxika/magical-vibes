package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneBombardment.class, Divination.class, DarkRitual.class})
class ArcaneBombardmentTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a random instant or sorcery from the graveyard and offers copies")
    void exilesGraveyardSpellAndOffersCopy() {
        UUID sourceId = addBombardment();
        Divination graveyardSpell = new Divination();
        harness.setGraveyard(player1, List.of(graveyardSpell));
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(sourceId).stream().map(Card::getId))
                .containsExactly(graveyardSpell.getId());
        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCopyIds()).hasSize(1);

        harness.handleMultipleCardsChosen(player1, choice.validCopyIds());

        assertThat(gd.stack).anyMatch(entry -> entry.isCopy()
                && entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("Triggers only for the first instant or sorcery spell each turn")
    void triggersOnlyOncePerTurn() {
        addBombardment();
        Divination graveyardSpell = new Divination();
        harness.setGraveyard(player1, List.of(graveyardSpell));
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType().name().equals("TRIGGERED_ABILITY")
                && entry.getCard().getName().equals("Arcane Bombardment"));
    }

    @Test
    @DisplayName("Copies the tracked pile even when the graveyard has no new instant or sorcery")
    void copiesTrackedPileWithoutNewGraveyardSpell() {
        UUID sourceId = addBombardment();
        Divination tracked = new Divination();
        harness.setGraveyard(player1, List.of(tracked));
        castDarkRitualAndDeclineCopies();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of());

        harness.passUntil(player1, TurnStep.CLEANUP);
        int priorityRounds = 0;
        while (!(gd.currentStep == TurnStep.PRECOMBAT_MAIN
                && gd.activePlayerId.equals(player1.getId()))) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice discard) {
                Player discardPlayer = discard.playerId().equals(player1.getId()) ? player1 : player2;
                harness.handleCardChosen(discardPlayer, discard.validIndices().getFirst());
            } else {
                harness.passBothPriorities();
            }
            assertThat(++priorityRounds).isLessThan(100);
        }
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(sourceId).stream().map(Card::getId))
                .containsExactly(tracked.getId());
        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCopyIds()).hasSize(1);
    }

    private UUID addBombardment() {
        harness.addToBattlefield(player1, new ArcaneBombardment());
        return harness.getPermanentId(player1, "Arcane Bombardment");
    }

    private void castDarkRitualAndDeclineCopies() {
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
    }

}
