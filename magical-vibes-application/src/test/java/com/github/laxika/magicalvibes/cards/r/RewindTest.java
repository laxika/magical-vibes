package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RewindTest extends BaseCardTest {

    private List<UUID> tappedIslandIds(Player player, int limit) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .filter(Permanent::isTapped)
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private long untappedIslands(Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .filter(p -> !p.isTapped())
                .count();
    }

    private void addTappedIslands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Island());
        }
        harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .forEach(Permanent::tap);
    }

    private void castRewindCounteringBears(GrizzlyBears bears) {
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Rewind()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Counters a creature spell and untaps the four chosen lands")
    void countersSpellAndUntapsChosenLands() {
        addTappedIslands(player2, 4);
        castRewindCounteringBears(new GrizzlyBears());

        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.handleMultiplePermanentsChosen(player2, tappedIslandIds(player2, 4));

        assertThat(untappedIslands(player2)).isEqualTo(4);
    }

    @Test
    @DisplayName("Offers a choice of at most four lands even if more are tapped")
    void offersAtMostFourLands() {
        addTappedIslands(player2, 6);
        castRewindCounteringBears(new GrizzlyBears());

        PendingInteraction.MultiPermanentChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(4);

        harness.handleMultiplePermanentsChosen(player2, tappedIslandIds(player2, 4));

        assertThat(untappedIslands(player2)).isEqualTo(4);
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .filter(Permanent::isTapped)
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("The controller may untap fewer lands than the maximum")
    void mayUntapFewerThanMaximum() {
        addTappedIslands(player2, 4);
        castRewindCounteringBears(new GrizzlyBears());

        harness.handleMultiplePermanentsChosen(player2, tappedIslandIds(player2, 2));

        assertThat(untappedIslands(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("The controller may decline to untap any land")
    void mayUntapNothing() {
        addTappedIslands(player2, 4);
        castRewindCounteringBears(new GrizzlyBears());

        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(untappedIslands(player2)).isZero();
    }

    @Test
    @DisplayName("Does not offer non-land permanents as untap choices")
    void doesNotUntapNonLandPermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        addTappedIslands(player2, 1);
        harness.getGameData().playerBattlefields.get(player2.getId()).forEach(Permanent::tap);

        castRewindCounteringBears(new GrizzlyBears());

        GameData gameData = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(findPermanent(player2, "Island").getId());

        harness.handleMultiplePermanentsChosen(player2, choice.validIds());

        assertThat(findPermanent(player2, "Island").isTapped()).isFalse();
        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    @DisplayName("No untap prompt appears when the controller has no tapped lands")
    void noPromptWithoutTappedLands() {
        harness.addToBattlefield(player2, new Island());

        castRewindCounteringBears(new GrizzlyBears());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }
}
