package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalamitysWakeTest extends BaseCardTest {

    private List<Integer> playableCards(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player, List.of(new GiantGrowth(), new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.clearPriorityPassed();
        harness.ensurePriority(player);
        return harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(harness.getGameData(), player.getId());
    }

    private void castWake() {
        harness.setHand(player1, List.of(new CalamitysWake()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("exiles all graveyards and itself")
    void exilesAllGraveyardsAndItself() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castWake();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Shock", "Calamity's Wake");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("locks noncreature spells for every player while allowing creature spells")
    void locksNoncreatureSpellsForEveryPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castWake();

        assertThat(playableCards(player1)).doesNotContain(0).contains(1);
        assertThat(playableCards(player2)).doesNotContain(0).contains(1);
    }

    @Test
    @DisplayName("noncreature spell lock ends with the turn")
    void noncreatureSpellLockEndsWithTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWake();
        assertThat(playableCards(player2)).doesNotContain(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(playableCards(player2)).contains(0);
    }
}
