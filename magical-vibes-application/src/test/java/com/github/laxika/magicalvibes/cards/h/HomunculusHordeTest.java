package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HomunculusHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates a token copy")
    void secondDrawCreatesTokenCopy() {
        Permanent horde = harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        addCardsToDeck(3);

        draw();
        assertThat(gd.stack).isEmpty();

        draw();
        resolveTopOfStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
        assertThat(horde.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("The trigger does not fire on later draws in the same turn")
    void triggersOnlyOnSecondDraw() {
        harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        addCardsToDeck(3);

        draw();
        draw();
        resolveTopOfStack();
        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    private void addCardsToDeck(int count) {
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        }
    }

    private void draw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
