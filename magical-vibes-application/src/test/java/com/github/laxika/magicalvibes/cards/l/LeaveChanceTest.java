package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeaveChance.class, GrizzlyBears.class, Island.class})
class LeaveChanceTest extends BaseCardTest {

    @Test
    @DisplayName("Leave returns any number of owned permanents to hand")
    void leaveReturnsOwnedPermanents() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new LeaveChance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(bear.getId(), island.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears") || c.getName().equals("Island"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Leave");
    }

    @Test
    @DisplayName("Leave can return zero targets")
    void leaveAllowsZeroTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeaveChance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Leave");
    }

    @Test
    @DisplayName("Leave cannot target a permanent you do not own")
    void leaveCannotTargetUnownedPermanent() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeaveChance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permanent you own");
    }

    @Test
    @DisplayName("Chance from graveyard discards then draws that many, then exiles")
    void chanceFlashbackDiscardsDrawsAndExiles() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new LeaveChance()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3); // 1 kept + 2 drawn
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Leave") || c.getName().equals("Chance"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leave"));
    }

    @Test
    @DisplayName("Chance requires sorcery timing")
    void chanceRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new LeaveChance()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    void leaveReturnsMoreThanNinetyNinePermanents() {
        var targets = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Island()).getId())
                .toList();
        harness.setHand(player1, List.of(new LeaveChance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, targets);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(100);
        harness.assertInGraveyard(player1, "Leave");
    }

    @Test
    void leaveReturnsOwnedPermanentControlledByOpponent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        gd.playerBattlefields.get(player1.getId()).remove(island);
        gd.playerBattlefields.get(player2.getId()).add(island);
        gd.stolenCreatures.put(island.getId(), player1.getId());
        harness.setHand(player1, List.of(new LeaveChance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(island.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(island);
        assertThat(gd.playerHands.get(player1.getId())).contains(island.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(island.getCard());
    }

    @Test
    void chanceCanDiscardZeroWithCardsInHand() {
        Island kept = new Island();
        Island undrawn = new Island();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(undrawn));
        harness.setGraveyard(player1, List.of(new LeaveChance()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leave"));
    }

    @Test
    void chanceWithEmptyHandDrawsNothingAndExiles() {
        Island undrawn = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(undrawn));
        harness.setGraveyard(player1, List.of(new LeaveChance()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leave"));
    }
}
