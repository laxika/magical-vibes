package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovetousCastaway.class, Island.class})
class CovetousCastawayTest extends BaseCardTest {

    @Test
    @DisplayName("Dies: mills three cards")
    void diesMillsThree() {
        Card top1 = new Island();
        Card top2 = new Island();
        Card top3 = new Island();
        Card top4 = new Island();
        harness.setLibrary(player1, List.of(top1, top2, top3, top4));
        Permanent castaway = harness.addToBattlefieldAndReturn(player1, new CovetousCastaway());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, castaway));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Covetous Castaway"))
                .anyMatch(c -> c.getId().equals(top1.getId()))
                .anyMatch(c -> c.getId().equals(top2.getId()))
                .anyMatch(c -> c.getId().equals(top3.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top4);
    }

    @Test
    @DisplayName("Disturb enters transformed as Ghostly Castigator")
    void disturbEntersTransformed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CovetousCastaway()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveFlashback(player1, 0, null); // resolve spell → ETB (empty GY, no prompt)
        harness.passBothPriorities(); // resolve empty ETB if on stack

        Permanent castigator = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(castigator.isTransformed()).isTrue();
        assertThat(castigator.getCard().getName()).isEqualTo("Ghostly Castigator");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ghostly Castigator ETB shuffles chosen graveyard cards into library")
    void castigatorEtbShufflesUpToThree() {
        Card gy1 = new Island();
        Card gy2 = new CovetousCastaway();
        Card gy3 = new Island();
        Card deckCard = new Island();
        harness.setLibrary(player1, List.of(deckCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CovetousCastaway(), gy1, gy2, gy3));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveFlashback(player1, 0, null); // resolve spell → ETB target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount())
                .isEqualTo(3);

        List<UUID> chosen = List.of(gy1.getId(), gy2.getId(), gy3.getId());
        harness.handleMultipleCardsChosen(player1, chosen);
        harness.passBothPriorities(); // resolve ETB
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(deckCard.getId(), gy1.getId(), gy2.getId(), gy3.getId());
        harness.assertOnBattlefield(player1, "Ghostly Castigator");
    }

    @Test
    @DisplayName("Ghostly Castigator is exiled instead of going to the graveyard")
    void castigatorExiledInsteadOfGraveyard() {
        Permanent castigator = putTransformedCastigatorOnBattlefield();
        UUID castigatorId = castigator.getOriginalCard().getId();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, castigator));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(castigatorId);
    }

    @Test
    void mayDeclineShuffleAfterChoosingTargets() {
        Card target = new Island();
        Card deckCard = new Island();
        harness.setLibrary(player1, List.of(deckCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CovetousCastaway(), target));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveFlashback(player1, 0, null);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(deckCard);
        harness.assertOnBattlefield(player1, "Ghostly Castigator");
    }

    @Test
    void canChooseZeroTargetsWithNonemptyGraveyard() {
        Card target = new Island();
        Card deckCard = new Island();
        harness.setLibrary(player1, List.of(deckCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CovetousCastaway(), target));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveFlashback(player1, 0, null);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(deckCard);
    }

    @Test
    void deathMillsOnlyAvailableCardsFromControllersLibrary() {
        Card remaining = new Island();
        Card opponentCard = new Island();
        harness.setLibrary(player1, List.of(remaining));
        harness.setLibrary(player2, List.of(opponentCard));
        Permanent castaway = harness.addToBattlefieldAndReturn(player1, new CovetousCastaway());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, castaway));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent putTransformedCastigatorOnBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CovetousCastaway()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveFlashback(player1, 0, null); // resolve spell
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MultiGraveyardChoice) {
            harness.handleMultipleCardsChosen(player1, List.of());
        }
        harness.passBothPriorities(); // resolve ETB
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
