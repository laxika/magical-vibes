package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunAshoreTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode puts the target on top of its owner's library")
    void firstModePutsTargetOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card existingTop = new Island();
        setDeck(player2, List.of(existingTop));

        cast(new int[]{0}, List.of(target.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), existingTop);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    @Test
    @DisplayName("The second mode returns the target to its owner's hand")
    void secondModeReturnsTargetToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{1}, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    @Test
    @DisplayName("Both modes resolve in order")
    void bothModesResolveInOrder() {
        Permanent libraryTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent handTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Card existingTop = new Island();
        setDeck(player2, List.of(existingTop));

        cast(new int[]{0, 1}, List.of(libraryTarget.getId(), handTarget.getId()));

        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryTarget.getCard(), existingTop);
        harness.assertInHand(player2, "Fountain of Youth");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Run Ashore");
    }

    @Test
    @DisplayName("Neither mode can target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new RunAshore()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    private void cast(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new RunAshore()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 6);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
