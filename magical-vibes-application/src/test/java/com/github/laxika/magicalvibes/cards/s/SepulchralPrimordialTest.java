package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.cards.g.GlaringSpotlight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SepulchralPrimordial.class, ArmoredTransport.class, GlaringSpotlight.class})
class SepulchralPrimordialTest extends BaseCardTest {

    private void castPrimordial() {
        harness.castFromHand(player1, new SepulchralPrimordial(), "{5}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB only offers creature cards from opponents' graveyards")
    void etbOnlyOffersOpponentCreatures() {
        Card opponentCreature = new ArmoredTransport();
        harness.setGraveyard(player2, List.of(opponentCreature, new GlaringSpotlight()));
        harness.setGraveyard(player1, List.of(new ArmoredTransport()));
        castPrimordial();

        List<UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).containsExactly(opponentCreature.getId());
    }

    @Test
    @DisplayName("Chosen creature enters under your control and stays (no exile at end step)")
    void reanimatesOpponentCreaturePermanently() {
        Card target = new ArmoredTransport();
        harness.setGraveyard(player2, List.of(target));
        castPrimordial();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent stolen = findPermanent(player1, "Armored Transport");
        assertThat(gd.stolenCreatures).containsKey(stolen.getId());
        assertThat(stolen.isTapped()).isFalse();
        harness.assertNotInGraveyard(player2, "Armored Transport");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Armored Transport");
    }

    @Test
    @DisplayName("Declining the up-to-one choice leaves the opponent's creature in their graveyard")
    void decliningLeavesCreatureInGraveyard() {
        Card target = new ArmoredTransport();
        harness.setGraveyard(player2, List.of(target));
        castPrimordial();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Armored Transport");
        harness.assertInGraveyard(player2, "Armored Transport");
    }

    @Test
    @DisplayName("ETB with no creature in an opponent's graveyard does not prompt")
    void etbNoValidTargetDoesNotPrompt() {
        harness.setGraveyard(player2, List.of(new GlaringSpotlight()));
        castPrimordial();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Sepulchral Primordial");
    }

    @Test
    @DisplayName("A selected target may still be declined when the trigger resolves")
    void canDeclineReanimationAtResolution() {
        Card target = new ArmoredTransport();
        harness.setGraveyard(player2, List.of(target));
        castPrimordial();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.assertInGraveyard(player2, "Armored Transport");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Armored Transport");
        harness.assertNotOnBattlefield(player1, "Armored Transport");
    }

    @Test
    @DisplayName("A target that leaves the graveyard before resolution is not returned")
    void targetLeavingGraveyardIsNotReturned() {
        Card target = new ArmoredTransport();
        Card other = new ArmoredTransport();
        harness.setGraveyard(player2, List.of(target, other));
        castPrimordial();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(other));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Armored Transport");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
    }
}
