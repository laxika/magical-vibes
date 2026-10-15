package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BubbleSmuggler;
import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoWitnesses.class, BubbleSmuggler.class, MagnifyingGlass.class, EsixFractalBloom.class})
class NoWitnessesTest extends BaseCardTest {

    @Test
    @DisplayName("The player with the most creatures investigates before all creatures are destroyed")
    void playerWithMostCreaturesInvestigatesThenAllCreaturesAreDestroyed() {
        harness.addToBattlefield(player1, new BubbleSmuggler());
        harness.addToBattlefield(player1, new BubbleSmuggler());
        harness.addToBattlefield(player2, new BubbleSmuggler());

        cast();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        harness.assertNotOnBattlefield(player1, "Bubble Smuggler");
        harness.assertNotOnBattlefield(player2, "Bubble Smuggler");
    }

    @Test
    @DisplayName("Every player tied for the most creatures investigates")
    void everyPlayerTiedForMostCreaturesInvestigates() {
        harness.addToBattlefield(player1, new BubbleSmuggler());
        harness.addToBattlefield(player2, new BubbleSmuggler());

        cast();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("All players investigate when no creatures are on the battlefield")
    void allPlayersInvestigateWhenCreatureCountsAreTiedAtZero() {
        cast();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Only the opponent investigates when they control the most creatures")
    void opponentWithMostCreaturesInvestigates() {
        harness.addToBattlefield(player2, new BubbleSmuggler());

        cast();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        harness.assertInGraveyard(player2, "Bubble Smuggler");
    }

    @Test
    @DisplayName("Noncreature artifacts neither count as creatures nor get destroyed")
    void noncreatureArtifactsDoNotAffectCreatureCountsAndSurvive() {
        harness.addToBattlefield(player1, new MagnifyingGlass());
        harness.addToBattlefield(player1, new MagnifyingGlass());
        harness.addToBattlefield(player2, new BubbleSmuggler());

        cast();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Magnifying Glass")).hasSize(2);
        harness.assertInGraveyard(player2, "Bubble Smuggler");
    }

    @Test
    @DisplayName("The opponent's Clue can be sacrificed for two mana to draw a card")
    void opponentCanUseTheirClueAfterTheWipe() {
        harness.addToBattlefield(player2, new BubbleSmuggler());
        BubbleSmuggler drawnCard = new BubbleSmuggler();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));

        cast();
        findPermanent(player2, "Clue").tap();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Declining a token replacement does not repeat another tied player's investigation")
    void decliningTokenReplacementDoesNotRepeatInvestigations() {
        harness.addToBattlefield(player1, new EsixFractalBloom());
        harness.addToBattlefield(player2, new BubbleSmuggler());

        cast();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        harness.assertInGraveyard(player1, "Esix, Fractal Bloom");
        harness.assertInGraveyard(player2, "Bubble Smuggler");
    }

    private void cast() {
        harness.setHand(player1, List.of(new NoWitnesses()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
