package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessRadrat.class})
class RuthlessRadratTest extends BaseCardTest {

    @Test
    @DisplayName("Squad exiles four graveyard cards per payment and creates matching token copies")
    void squadExilesCardsAndCreatesCopies() {
        List<Card> graveyard = List.of(
                new RuthlessRadrat(), new RuthlessRadrat(), new RuthlessRadrat(), new RuthlessRadrat(),
                new RuthlessRadrat(), new RuthlessRadrat(), new RuthlessRadrat(), new RuthlessRadrat());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new RuthlessRadrat()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithRepeatedCostsAndGraveyardExile(
                player1, 0, List.of("{0}", "{0}"), List.of(0, 1, 2, 3, 4, 5, 6, 7));
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> radrats = findPermanents(player1, "Ruthless Radrat");
        assertThat(radrats).hasSize(3);
        assertThat(radrats).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(8);
    }

    @Test
    @DisplayName("Squad is optional")
    void squadMayBePaidZeroTimes() {
        harness.setHand(player1, List.of(new RuthlessRadrat()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ruthless Radrat")).hasSize(1);
    }

    @Test
    @DisplayName("Squad rejects a payment without enough graveyard cards")
    void squadRequiresFourCardsPerPayment() {
        harness.setGraveyard(player1, List.of(new RuthlessRadrat(), new RuthlessRadrat(), new RuthlessRadrat()));
        harness.setHand(player1, List.of(new RuthlessRadrat()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCostsAndGraveyardExile(
                player1, 0, List.of("{0}"), List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must exile exactly 4 cards");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Squad does not trigger when the creature enters without being cast")
    void enteringWithoutCastingDoesNotTriggerSquad() {
        harness.enterBattlefieldAndReturn(player1, new RuthlessRadrat());

        assertThat(findPermanents(player1, "Ruthless Radrat")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Squad cannot exile the same graveyard card more than once")
    void squadRejectsDuplicateExileSelections() {
        harness.setGraveyard(player1, List.of(
                new RuthlessRadrat(), new RuthlessRadrat(), new RuthlessRadrat(), new RuthlessRadrat()));
        harness.setHand(player1, List.of(new RuthlessRadrat()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCostsAndGraveyardExile(
                player1, 0, List.of("{0}"), List.of(0, 0, 1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate graveyard card indices");

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInHand(player1, "Ruthless Radrat");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Squad pays its exile cost while casting and leaves unselected cards alone")
    void squadExilesSelectedCardsBeforeResolution() {
        Card unselected = new RuthlessRadrat();
        harness.setGraveyard(player1, List.of(
                unselected, new RuthlessRadrat(), new RuthlessRadrat(),
                new RuthlessRadrat(), new RuthlessRadrat()));
        harness.setHand(player1, List.of(new RuthlessRadrat()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithRepeatedCostsAndGraveyardExile(
                player1, 0, List.of("{0}"), List.of(4, 2, 1, 3));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
        assertThat(gd.exiledCards).hasSize(4);
        assertThat(findPermanents(player1, "Ruthless Radrat")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ruthless Radrat")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new RuthlessRadrat());
        addCreatureReady(player2, new RuthlessRadrat());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two blockers")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new RuthlessRadrat());
        Permanent firstBlocker = addCreatureReady(player2, new RuthlessRadrat());
        Permanent secondBlocker = addCreatureReady(player2, new RuthlessRadrat());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
