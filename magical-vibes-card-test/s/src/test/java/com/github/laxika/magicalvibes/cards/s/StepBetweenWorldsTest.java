package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BristlyBillSpineSower;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StepBetweenWorlds.class, BristlyBillSpineSower.class})
class StepBetweenWorldsTest extends BaseCardTest {

    @Test
    @DisplayName("Players choose independently, then only accepters shuffle and draw seven")
    void eachPlayerChoosesIndependently() {
        Card player1HandCard = new BristlyBillSpineSower();
        Card player2HandCard = new BristlyBillSpineSower();
        harness.setHand(player1, List.of(new StepBetweenWorlds(), player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castStepBetweenWorlds();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1HandCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Step Between Worlds"));
    }

    @Test
    @DisplayName("All choices happen before any accepted player's zones change")
    void choicesCompleteBeforeResolution() {
        Card player1HandCard = new BristlyBillSpineSower();
        Card player2HandCard = new BristlyBillSpineSower();
        harness.setHand(player1, List.of(new StepBetweenWorlds(), player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castStepBetweenWorlds();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1HandCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2HandCard);
    }

    @Test
    @DisplayName("A player may accept with an empty hand and graveyard")
    void emptyZonesCanStillBeAccepted() {
        harness.setHand(player1, List.of(new StepBetweenWorlds()));
        harness.setHand(player2, List.of());
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castStepBetweenWorlds();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    void bothPlayersShuffleTheirHandAndGraveyardAndDrawSeven() {
        StepBetweenWorlds spell = new StepBetweenWorlds();
        Card firstHand = new BristlyBillSpineSower();
        Card secondHand = new BristlyBillSpineSower();
        Card firstGraveyard = new BristlyBillSpineSower();
        Card secondGraveyard = new BristlyBillSpineSower();
        harness.setHand(player1, List.of(spell, firstHand));
        harness.setHand(player2, List.of(secondHand));
        harness.setGraveyard(player1, List.of(firstGraveyard));
        harness.setGraveyard(player2, List.of(secondGraveyard));
        fillLibrary(player1, 5);
        fillLibrary(player2, 5);

        castStepBetweenWorlds();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7)
                .contains(firstHand, firstGraveyard).doesNotContain(spell);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7)
                .contains(secondHand, secondGraveyard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    void bothPlayersDecliningLeavesTheirZonesUnchangedAndStillExilesSpell() {
        StepBetweenWorlds spell = new StepBetweenWorlds();
        Card handCard = new BristlyBillSpineSower();
        Card graveyardCard = new BristlyBillSpineSower();
        harness.setHand(player1, List.of(spell, handCard));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castStepBetweenWorlds();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    void plottedSpellCanBeCastForFreeOnALaterTurnAndIsExiledWithoutAnotherPermission() {
        StepBetweenWorlds spell = new StepBetweenWorlds();
        harness.setHand(player1, List.of(spell));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castStepBetweenWorlds() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void fillLibrary(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new BristlyBillSpineSower());
        }
        harness.setLibrary(player, cards);
    }
}
