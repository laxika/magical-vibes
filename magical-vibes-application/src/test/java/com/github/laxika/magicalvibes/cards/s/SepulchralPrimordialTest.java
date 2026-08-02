package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SepulchralPrimordialTest extends BaseCardTest {

    private void castPrimordial() {
        harness.setHand(player1, List.of(new SepulchralPrimordial()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB only offers creature cards from opponents' graveyards")
    void etbOnlyOffersOpponentCreatures() {
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCreature, new Island()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castPrimordial();

        List<UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).containsExactly(opponentCreature.getId());
    }

    @Test
    @DisplayName("Chosen creature enters under your control and stays (no exile at end step)")
    void reanimatesOpponentCreaturePermanently() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        castPrimordial();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        Permanent stolen = findCreatureOnBattlefield(player1.getId(), "Grizzly Bears");
        assertThat(gd.stolenCreatures).containsKey(stolen.getId());
        assertThat(stolen.isTapped()).isFalse();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the up-to-one choice leaves the opponent's creature in their graveyard")
    void decliningLeavesCreatureInGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        castPrimordial();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB with no creature in an opponent's graveyard does not prompt")
    void etbNoValidTargetDoesNotPrompt() {
        harness.setGraveyard(player2, List.of(new Island()));
        castPrimordial();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Sepulchral Primordial");
    }

    private Permanent findCreatureOnBattlefield(UUID playerId, String cardName) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(cardName + " not found on battlefield"));
    }
}
