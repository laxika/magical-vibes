package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FountainportBell.class, Forest.class, BarkformHarvester.class, Plains.class})
class FountainportBellTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield may put a basic land on top of the library")
    void enteringMayPutBasicLandOnTop() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        BarkformHarvester harvester = new BarkformHarvester();
        harness.setLibrary(player1, List.of(harvester, plains, forest));
        castBell();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards();
        assertThat(offered).containsExactlyInAnyOrder(plains, forest);

        int chosenIndex = offered.indexOf(plains);
        harness.handleCardChosen(player1, chosenIndex);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(plains);
    }

    @Test
    @DisplayName("Declining the enters-the-battlefield search does nothing")
    void decliningSearchDoesNothing() {
        harness.setLibrary(player1, List.of(new Plains()));
        castBell();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing the bell draws a card")
    void sacrificingBellDrawsCard() {
        Card drawn = new BarkformHarvester();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new Plains()));
        Permanent bell = harness.addToBattlefieldAndReturn(player1, new FountainportBell());
        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1).contains(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bell);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bell.getCard());
    }

    @Test
    @DisplayName("A basic land search may fail to find even when a basic land is present")
    void mayFailToFindBasicLand() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        castBell();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Searching with no basic lands completes without taking a card")
    void searchWithNoBasicLandsCompletes() {
        Card nonland = new BarkformHarvester();
        harness.setLibrary(player1, List.of(nonland));
        castBell();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }

    @Test
    @DisplayName("Sacrificing the bell before its enter trigger resolves draws first and preserves the search")
    void sacrificeInResponseToEnterTrigger() {
        Card drawn = new BarkformHarvester();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(drawn, plains));
        castBell();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Fountainport Bell");
        harness.assertInGraveyard(player1, "Fountainport Bell");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castBell() {
        harness.setHand(player1, List.of(new FountainportBell()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
    }
}
