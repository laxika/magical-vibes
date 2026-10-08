package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YavimayaDryad.class, Forest.class})
class YavimayaDryadTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability targets a player")
    void etbAbilityTargetsAPlayer() {
        castDryad();

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("The accepted search puts a Forest tapped under the target player's control")
    void acceptedSearchPutsForestUnderTargetPlayersControl() {
        castDryad();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        Permanent forest = findPermanent(player2, "Forest");
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the search does not put a Forest onto the battlefield")
    void decliningSearchDoesNothing() {
        castDryad();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void canPutForestUnderOwnControlAndSearchOnlyOwnLibrary() {
        castDryad();
        Forest ownForest = new Forest();
        Forest opposingForest = new Forest();
        harness.setLibrary(player1, List.of(ownForest));
        harness.setLibrary(player2, List.of(opposingForest));

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").getCard()).isSameAs(ownForest);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingForest);
    }

    @Test
    void canFailToFindEvenWhenForestIsAvailable() {
        castDryad();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    void searchDoesNotFindNonForestCards() {
        castDryad();
        YavimayaDryad otherDryad = new YavimayaDryad();
        harness.setLibrary(player1, List.of(otherDryad));

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherDryad);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    void acceptedSearchWithEmptyLibraryCompletes() {
        castDryad();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    void forestwalkPreventsBlockingEvenWithTappedForest() {
        harness.addToBattlefieldAndReturn(player2, new Forest()).tap();
        Permanent blocker = addCreatureReady(player2, new YavimayaDryad());
        addCreatureReady(player1, new YavimayaDryad());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void forestwalkDoesNotPreventBlockingWhenOnlyAttackerControlsForest() {
        Permanent attacker = addCreatureReady(player1, new YavimayaDryad());
        harness.addToBattlefield(player1, new Forest());
        Permanent blocker = addCreatureReady(player2, new YavimayaDryad());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0,
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castDryad() {
        harness.setHand(player1, List.of(new YavimayaDryad()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }
}
