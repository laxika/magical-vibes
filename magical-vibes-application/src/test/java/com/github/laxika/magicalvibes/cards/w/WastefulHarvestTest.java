package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BoulderbranchGolem;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WastefulHarvest.class, Forest.class, Shock.class, BoulderbranchGolem.class})
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
        harness.setLibrary(player1, List.of(cards));
    }

    @Test
    @DisplayName("Accepting one permanent removes the remaining return offers")
    void returnsAtMostOnePermanent() {
        Forest first = new Forest();
        BoulderbranchGolem second = new BoulderbranchGolem();
        harness.setLibrary(player1, List.of(first, second, new Shock(), new Shock(), new Shock()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wasteful Harvest");
    }

    @Test
    @DisplayName("Declining the first permanent permits returning a later artifact creature")
    void canChooseLaterPermanent() {
        Forest first = new Forest();
        BoulderbranchGolem second = new BoulderbranchGolem();
        harness.setLibrary(player1, List.of(first, second, new Shock(), new Shock(), new Shock()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cards already in the graveyard are not eligible for return")
    void cannotReturnPreexistingPermanent() {
        Forest oldCard = new Forest();
        harness.setGraveyard(player1, List.of(oldCard));
        setTopFive(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolveToMay();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldCard).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A short library is milled completely and its permanent can still be returned")
    void resolvesWithFewerThanFiveCards() {
        Forest permanent = new Forest();
        harness.setLibrary(player1, List.of(permanent, new Shock()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(permanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the top five cards of the controller's library are milled")
    void leavesSixthCardAndOpponentsLibraryUntouched() {
        Forest sixth = new Forest();
        Forest opponentCard = new Forest();
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), sixth));
        harness.setLibrary(player2, List.of(opponentCard));

        castAndResolveToMay();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library resolves without offering a return")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castAndResolveToMay();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Wasteful Harvest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
