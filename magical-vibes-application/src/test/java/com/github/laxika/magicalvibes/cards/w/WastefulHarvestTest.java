package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WastefulHarvestTest extends BaseCardTest {

    private void castAndResolveToMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WastefulHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Mills five cards then prompts to return a milled permanent")
    void millsThenMayPrompt() {
        setTopFive(new Forest(), new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolveToMay();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may returns a milled permanent to hand")
    void acceptingMayReturnsMilledPermanent() {
        setTopFive(new Forest(), new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining may leaves milled cards in the graveyard")
    void decliningMayLeavesMilledCards() {
        setTopFive(new Forest(), new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not offer a may when no permanent was milled")
    void noPermanentMilled() {
        setTopFive(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolveToMay();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Shock");
    }

    private void setTopFive(com.github.laxika.magicalvibes.model.Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
