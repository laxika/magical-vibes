package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReadTheTides.class, AlpineWatchdog.class, Forest.class, Island.class})
class ReadTheTidesTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards when the draw mode is chosen")
    void drawsThreeCards() {
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(c -> c.getName().equals("Forest"))
                .hasSize(3);
        harness.assertInGraveyard(player1, "Read the Tides");
    }

    @Test
    @DisplayName("Returns up to two target creatures to their owners' hands")
    void returnsUpToTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Alpine Watchdog"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Returns two target creatures when both are chosen")
    void returnsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Alpine Watchdog"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Bounce mode cannot target a noncreature permanent")
    void bounceModeRejectsNoncreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures");
    }

    @Test
    @DisplayName("Bounce mode can resolve with zero targets and does not draw")
    void bounceModeWithZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Read the Tides");
    }

    @Test
    @DisplayName("Bounce mode can return creatures controlled by different players")
    void returnsCreaturesOfBothPlayers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castModalSorcery(player1, 0, 1, List.of(own.getId(), opposing.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(own.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposing.getCard());
    }

    @Test
    @DisplayName("The remaining legal target is returned if the other target leaves")
    void returnsRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castModalSorcery(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Read the Tides");
    }

    @Test
    @DisplayName("Bounce mode rejects choosing the same creature twice")
    void rejectsDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bounce mode rejects more than two creatures")
    void rejectsThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature controlled by an opponent returns to its owner's hand")
    void returnsToOwnerRatherThanController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castModalSorcery(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature.getCard());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Bounce mode does not switch to drawing when its only target leaves")
    void doesNotDrawWhenAllTargetsBecomeIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new ReadTheTides()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castModalSorcery(player1, 0, 1, List.of(creature.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature.getCard());
        harness.assertInGraveyard(player1, "Read the Tides");
    }
}
