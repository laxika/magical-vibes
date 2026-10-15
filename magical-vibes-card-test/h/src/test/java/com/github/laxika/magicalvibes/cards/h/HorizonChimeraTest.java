package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HorizonChimera.class})
class HorizonChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card gains 1 life")
    void drawingCardGainsLife() {
        harness.addToBattlefield(player1, new HorizonChimera());
        harness.setLibrary(player1, List.of(new HorizonChimera()));
        int lifeBefore = gd.getLife(player1.getId());

        drawAndResolveTrigger(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Each card drawn gains 1 life")
    void gainsLifeForEachCardDrawn() {
        harness.addToBattlefield(player1, new HorizonChimera());
        harness.setLibrary(player1, List.of(new HorizonChimera(), new HorizonChimera()));
        int lifeBefore = gd.getLife(player1.getId());

        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("An opponent drawing a card does not trigger Horizon Chimera")
    void opponentDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new HorizonChimera());
        harness.setLibrary(player2, List.of(new HorizonChimera()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawingMultipleCardsQueuesSeparateTriggers() {
        harness.addToBattlefield(player1, new HorizonChimera());
        harness.setLibrary(player1, List.of(new HorizonChimera(), new HorizonChimera()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void eachChimeraTriggersIndependently() {
        harness.addToBattlefield(player1, new HorizonChimera());
        harness.addToBattlefield(player1, new HorizonChimera());
        harness.setLibrary(player1, List.of(new HorizonChimera()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void triggerStillGainsLifeAfterChimeraLeavesBattlefield() {
        HorizonChimera chimera = new HorizonChimera();
        harness.addToBattlefield(player1, chimera);
        harness.setLibrary(player1, List.of(new HorizonChimera()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).add(chimera);

        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HorizonChimera()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        gs.passPriority(gd, player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Horizon Chimera")).isEqualTo(1);
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        resolveAllTriggers();
    }
}
