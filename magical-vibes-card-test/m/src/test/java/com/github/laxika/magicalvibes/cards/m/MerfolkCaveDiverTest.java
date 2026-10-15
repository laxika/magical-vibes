package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CenoteScout;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkCaveDiver.class, CenoteScout.class, Forest.class})
class MerfolkCaveDiverTest extends BaseCardTest {

    @Test
    @DisplayName("When a creature explores, Merfolk Cave-Diver gets +1/+0 and can't be blocked")
    void exploreTriggersBoostAndUnblockable() {
        Permanent caveDiver = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        assertThat(caveDiver.getPowerModifier()).isEqualTo(1);
        assertThat(caveDiver.getToughnessModifier()).isZero();
        assertThat(caveDiver.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Explore trigger resolves after the nonland choice")
    void exploreNonlandTriggersAfterChoice() {
        Permanent caveDiver = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        gd.playerDecks.get(player1.getId()).addFirst(new MerfolkCaveDiver());

        castExplorerAndResolveExplore();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(caveDiver.getPowerModifier()).isEqualTo(1);
        assertThat(caveDiver.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The boost and unblockability wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent caveDiver = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(caveDiver.getPowerModifier()).isZero();
        assertThat(caveDiver.getToughnessModifier()).isZero();
        assertThat(caveDiver.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Repeated explores give cumulative power boosts")
    void repeatedExploresAccumulateBoosts() {
        Permanent caveDiver = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        castExplorerAndResolveExplore();
        harness.passBothPriorities();
        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        assertThat(caveDiver.getPowerModifier()).isEqualTo(2);
        assertThat(caveDiver.getToughnessModifier()).isZero();
        assertThat(caveDiver.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Each Cave-Diver receives its own explore trigger")
    void eachCaveDiverTriggersIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        harness.setLibrary(player1, List.of(new Forest()));

        castExplorerAndResolveExplore();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("An opponent's exploring creature does not trigger your Cave-Diver")
    void opponentsExploreDoesNotTrigger() {
        Permanent caveDiver = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        Permanent opposingDiver = harness.addToBattlefieldAndReturn(player2, new MerfolkCaveDiver());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castExplorerAndResolveExplore(player2);
        harness.passBothPriorities();

        assertThat(caveDiver.getPowerModifier()).isZero();
        assertThat(caveDiver.isCantBeBlocked()).isFalse();
        assertThat(opposingDiver.getPowerModifier()).isEqualTo(1);
        assertThat(opposingDiver.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Exploring an empty library still triggers Cave-Diver")
    void emptyLibraryStillTriggers() {
        Permanent caveDiver = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        harness.setLibrary(player1, List.of());

        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        assertThat(caveDiver.getPowerModifier()).isEqualTo(1);
        assertThat(caveDiver.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Putting the explored nonland into the graveyard also triggers Cave-Diver")
    void graveyardChoiceCompletesExploreAndTriggers() {
        Permanent caveDiver = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        MerfolkCaveDiver revealedCard = new MerfolkCaveDiver();
        harness.setLibrary(player1, List.of(revealedCard));

        castExplorerAndResolveExplore();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(caveDiver.getPowerModifier()).isZero();
        assertThat(caveDiver.isCantBeBlocked()).isFalse();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealedCard);
        assertThat(caveDiver.getPowerModifier()).isEqualTo(1);
        assertThat(caveDiver.isCantBeBlocked()).isTrue();
    }

    private void castExplorerAndResolveExplore() {
        castExplorerAndResolveExplore(player1);
    }

    private void castExplorerAndResolveExplore(Player player) {
        harness.castFromHand(player, new CenoteScout(), "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
