package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheInfamousCruelclaw.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class})
class TheInfamousCruelclawTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles until a nonland card and casts it by discarding a card")
    void castsExiledNonlandByDiscarding() {
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        Forest land = new Forest();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, spell, new Forest(), new Forest()));
        harness.setHand(player1, List.of(discarded));

        attackWithCruelclaw();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, spell);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Declining leaves the nonland card and intermediate lands in exile")
    void decliningLeavesCardsInExile() {
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        Forest land = new Forest();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, spell));
        harness.setHand(player1, List.of(discarded));

        attackWithCruelclaw();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, spell);
    }

    @Test
    @DisplayName("Does not offer the cast when the controller has no card to discard")
    void noDiscardAvailableLeavesCardsInExile() {
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, spell));
        harness.setHand(player1, List.of());

        attackWithCruelclaw();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, spell);
    }

    private void attackWithCruelclaw() {
        Permanent cruelclaw = addCreatureReady(player1, new TheInfamousCruelclaw());
        cruelclaw.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
