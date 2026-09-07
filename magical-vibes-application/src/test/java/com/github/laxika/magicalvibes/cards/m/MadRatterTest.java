package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MadRatter.class, GrizzlyBears.class})
class MadRatterTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates two Rat tokens")
    void secondDrawCreatesTwoRatTokens() {
        harness.addToBattlefieldAndReturn(player1, new MadRatter());
        addCardsToDeck(2);

        draw();
        assertThat(gd.stack).isEmpty();

        draw();
        resolveTopOfStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger does not fire on later draws in the same turn")
    void triggersOnlyOnSecondDraw() {
        Permanent ratter = harness.addToBattlefieldAndReturn(player1, new MadRatter());
        addCardsToDeck(3);

        draw();
        draw();
        resolveTopOfStack();
        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(ratter.getCard().isToken()).isFalse();
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
