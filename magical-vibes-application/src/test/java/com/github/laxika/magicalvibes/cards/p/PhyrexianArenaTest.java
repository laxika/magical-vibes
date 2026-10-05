package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PhyrexianArena.class)
class PhyrexianArenaTest extends BaseCardTest {

    @Test
    @DisplayName("Draw and life loss wait for the upkeep ability to resolve")
    void effectsWaitForResolution() {
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.setLibrary(player1, List.of(new PhyrexianArena()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertLife(player1, lifeBefore);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Upkeep ability still resolves after Arena leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.setLibrary(player1, List.of(new PhyrexianArena()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        var arena = findPermanent(player1, "Phyrexian Arena");
        gd.playerBattlefields.get(player1.getId()).remove(arena);
        gd.playerGraveyards.get(player1.getId()).add(arena.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player1, lifeBefore - 1);
        harness.assertNotOnBattlefield(player1, "Phyrexian Arena");
    }

    @Test
    @DisplayName("Second player's Arena draws and loses life only for that player")
    void benefitsAndLifeLossGoToSecondPlayer() {
        harness.addToBattlefield(player2, new PhyrexianArena());
        harness.setLibrary(player2, List.of(new PhyrexianArena()));
        int firstHandBefore = gd.playerHands.get(player1.getId()).size();
        int secondHandBefore = gd.playerHands.get(player2.getId()).size();
        int firstLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int secondLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(secondHandBefore + 1);
        harness.assertLife(player2, secondLifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(firstHandBefore);
        harness.assertLife(player1, firstLifeBefore);
    }

    @Test
    @DisplayName("Controller draws a card and loses 1 life at upkeep")
    void drawsAndLosesLifeAtUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.setLibrary(player1, List.of(new PhyrexianArena()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Phyrexian Arena");
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Each copy triggers independently during its controller's upkeep")
    void eachCopyTriggersIndependently() {
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.setLibrary(player1, List.of(new PhyrexianArena(), new PhyrexianArena()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianArena());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Each Arena triggers independently during its controller's upkeep")
    void eachArenaTriggersIndependently() {
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.setLibrary(player1, List.of(new PhyrexianArena(), new PhyrexianArena()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
        harness.assertLife(player1, lifeBefore - 2);
    }
}
