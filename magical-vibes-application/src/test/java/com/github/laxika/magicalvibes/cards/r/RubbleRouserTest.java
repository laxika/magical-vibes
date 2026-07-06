package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RubbleRouserTest extends BaseCardTest {

    // ===== ETB rummage =====

    @Test
    @DisplayName("ETB: accepting discards a card and draws a card")
    void etbAcceptDiscardsAndDraws() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, new ArrayList<>(List.of(new RubbleRouser(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB
        harness.passBothPriorities(); // resolve ETB → may prompt

        harness.handleMayAbilityChosen(player1, true);
        // Discard the Grizzly Bears
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Mana ability: exile a graveyard card, add {R}, deal 1 to each opponent")
    void manaAbilityAddsRedAndDamagesOpponents() {
        Permanent rouser = addCreatureReady(player1, new RubbleRouser());
        Card gyCard = new LlanowarElves();
        harness.setGraveyard(player1, List.of(gyCard));

        harness.activateAbility(player1, 0, 0, null, null);
        // Choose which graveyard card to exile as the cost
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        // {R} added to the pool
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        // Opponent takes 1 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        // Cost card exiled from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(gyCard);
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == gyCard);
        // Source tapped
        assertThat(rouser.isTapped()).isTrue();
    }
}
