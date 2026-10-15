package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarrowingJourney.class, BlackCat.class})
class HarrowingJourneyTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws three cards and loses 3 life")
    void targetPlayerDrawsThreeCardsAndLoses3Life() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        resolveHarrowingJourneyTargeting(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        resolveHarrowingJourneyTargeting(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        resolveHarrowingJourneyTargeting(player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        resolveHarrowingJourneyTargeting(player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Harrowing Journey");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BlackCat());

        harness.setHand(player1, List.of(new HarrowingJourney()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draws all three cards before lethal life loss")
    void drawsAllThreeCardsBeforeLethalLifeLoss() {
        BlackCat first = new BlackCat();
        BlackCat second = new BlackCat();
        BlackCat third = new BlackCat();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLife(player2, 3);

        resolveHarrowingJourneyTargeting(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 0);
        harness.assertLife(player1, 20);
    }

    private void resolveHarrowingJourneyTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new HarrowingJourney()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }
}
