package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HorizonChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card gains 1 life")
    void drawingCardGainsLife() {
        harness.addToBattlefield(player1, new HorizonChimera());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        drawAndResolveTrigger(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Each card drawn gains 1 life")
    void gainsLifeForEachCardDrawn() {
        harness.addToBattlefield(player1, new HorizonChimera());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("An opponent drawing a card does not trigger Horizon Chimera")
    void opponentDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new HorizonChimera());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
