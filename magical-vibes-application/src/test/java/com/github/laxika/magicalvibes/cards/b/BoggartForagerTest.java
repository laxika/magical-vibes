package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoggartForager.class})
class BoggartForagerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating puts a player-targeting ability on the stack and sacrifices the creature")
    void activatePutsAbilityOnStack() {
        harness.addToBattlefield(player1, new BoggartForager());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player1.getId());

        // Boggart Forager is sacrificed as a cost
        harness.assertNotOnBattlefield(player1, "Boggart Forager");
        harness.assertInGraveyard(player1, "Boggart Forager");

        // Ability is on the stack targeting the chosen player
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving shuffles the targeted controller's library")
    void resolvingShufflesOwnLibrary() {
        harness.addToBattlefield(player1, new BoggartForager());
        harness.addMana(player1, ManaColor.RED, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        // Shuffle does not change library size
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Can target an opponent to shuffle their library")
    void canTargetOpponent() {
        harness.addToBattlefield(player1, new BoggartForager());
        harness.addMana(player1, ManaColor.RED, 1);

        int opponentDeckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSizeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Cannot activate without the required mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new BoggartForager());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        var forager = harness.addToBattlefieldAndReturn(player1, new BoggartForager());
        forager.setSummoningSick(true);
        forager.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boggart Forager");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target and shuffle an empty library")
    void canShuffleEmptyLibrary() {
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new BoggartForager());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.equals(player2.getUsername() + " shuffles their library."));
    }

    @Test
    @DisplayName("Shuffling preserves all library cards and leaves the other library untouched")
    void shufflesOnlyTargetLibrary() {
        List<BoggartForager> ownLibrary = List.of(new BoggartForager(), new BoggartForager());
        List<BoggartForager> opponentLibrary = List.of(new BoggartForager(), new BoggartForager(), new BoggartForager());
        harness.setLibrary(player1, ownLibrary);
        harness.setLibrary(player2, opponentLibrary);
        harness.addToBattlefield(player1, new BoggartForager());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(ownLibrary);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrderElementsOf(opponentLibrary);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.equals(player2.getUsername() + " shuffles their library."));
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a player")
    void cannotTargetPermanent() {
        var forager = harness.addToBattlefieldAndReturn(player1, new BoggartForager());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forager.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Boggart Forager");
        harness.assertNotInGraveyard(player1, "Boggart Forager");
        assertThat(gd.stack).isEmpty();
    }
}
