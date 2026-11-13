package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TuinvaleTreefolk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharmingPrince.class, TuinvaleTreefolk.class})
class CharmingPrinceTest extends BaseCardTest {

    private static final String SCRY_MODE = "Scry 2.";
    private static final String LIFE_MODE = "You gain 3 life.";
    private static final String FLICKER_MODE =
            "Exile another target creature you own. Return it to the battlefield under your control at the beginning of the next end step.";

    @Test
    @DisplayName("Scry mode opens a two-card scry choice")
    void scryMode() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.add(0, new TuinvaleTreefolk());
        deck.add(1, new TuinvaleTreefolk());

        castPrince(SCRY_MODE, null);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Gain-life mode gains 3 life")
    void gainLifeMode() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castPrince(LIFE_MODE, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Flicker mode can target an owned creature controlled by an opponent")
    void flickerOwnedCreatureControlledByOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TuinvaleTreefolk());
        gd.stolenCreatures.put(target.getId(), player1.getId());

        castPrince(FLICKER_MODE, target.getId());

        harness.assertNotOnBattlefield(player2, "Tuinvale Treefolk");
        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tuinvale Treefolk");
    }

    @Test
    @DisplayName("Scry mode can reorder the top cards and put a card on the bottom")
    void scryCanPutCardOnBottom() {
        Card first = new TuinvaleTreefolk();
        Card second = new TuinvaleTreefolk();
        Card third = new TuinvaleTreefolk();
        harness.setLibrary(player1, List.of(first, second, third));

        castPrince(SCRY_MODE, null);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    @DisplayName("Scry mode works with fewer than two cards in the library")
    void scryWithOneCard() {
        Card card = new TuinvaleTreefolk();
        harness.setLibrary(player1, List.of(card));

        castPrince(SCRY_MODE, null);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("Scry mode works with an empty library")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castPrince(SCRY_MODE, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Charming Prince");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flicker return uses the stack and returns the creature as a new permanent")
    void flickerReturnUsesStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TuinvaleTreefolk());
        target.tap();

        castPrince(FLICKER_MODE, target.getId());

        harness.assertNotOnBattlefield(player1, "Tuinvale Treefolk");
        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Tuinvale Treefolk");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tuinvale Treefolk"))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flicker mode rejects a creature the controller does not own")
    void flickerUnownedCreatureRejected() {
        harness.addToBattlefieldAndReturn(player2, new TuinvaleTreefolk());

        harness.setHand(player1, List.of(new CharmingPrince()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, FLICKER_MODE))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertOnBattlefield(player2, "Tuinvale Treefolk");
    }

    private void castPrince(String mode, UUID targetId) {
        castPrinceChoosingMode(mode);
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
        harness.passBothPriorities();
    }

    private void castPrinceChoosingMode(String mode) {
        harness.setHand(player1, List.of(new CharmingPrince()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }

    private void advanceToEndStep() {
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
