package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinInTheirWake.class, Forest.class, Wastes.class})
class RuinInTheirWakeTest extends BaseCardTest {

    @Test
    @DisplayName("Without Wastes, the revealed basic land goes into hand")
    void withoutWastesPutsBasicLandIntoHand() {
        Card forest = new Forest();
        castRuin(forest);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With a Wastes, the revealed basic land may enter the battlefield tapped")
    void withWastesMayPutBasicLandOntoBattlefieldTapped() {
        harness.addToBattlefield(player1, new Wastes());
        Card forest = new Forest();
        castRuin(forest);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
    }

    @Test
    @DisplayName("Declining the Wastes battlefield option leaves the basic land in hand")
    void decliningBattlefieldOptionPutsBasicLandIntoHand() {
        harness.addToBattlefield(player1, new Wastes());
        Card forest = new Forest();
        castRuin(forest);

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest);
    }

    @Test
    @DisplayName("An opponent's Wastes does not enable the battlefield option")
    void opponentsWastesDoesNotEnableBattlefieldOption() {
        harness.addToBattlefield(player2, new Wastes());
        Card wastes = new Wastes();
        castRuin(wastes);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wastes);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Wastes");
    }

    @Test
    @DisplayName("Finding Wastes does not itself enable the battlefield option")
    void findingWastesWithoutControllingOnePutsItIntoHand() {
        Card wastes = new Wastes();
        castRuin(wastes);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wastes);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Wastes");
    }

    @Test
    @DisplayName("A filtered search may fail to find even with a basic land available")
    void mayFailToFindWithWastesAndBasicLandAvailable() {
        harness.addToBattlefield(player1, new Wastes());
        Card wastes = new Wastes();
        castRuin(wastes);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wastes);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The revealed land does not enter hand before the battlefield decision")
    void selectedLandDoesNotEnterHandBeforeDestinationIsChosen() {
        harness.addToBattlefield(player1, new Wastes());
        Card wastes = new Wastes();
        castRuin(wastes);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(wastes);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == wastes && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(wastes);
    }

    private void castRuin(Card forest) {
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new RuinInTheirWake()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }
}
