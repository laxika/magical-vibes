package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TigerSeal.class, GrizzlyBears.class})
class TigerSealTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself at the beginning of its controller's upkeep")
    void tapsAtBeginningOfUpkeep() {
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new TigerSeal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(seal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps itself when its controller draws their second card")
    void untapsOnSecondCardDraw() {
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new TigerSeal());
        seal.tap();
        harness.setLibrary(player1, java.util.List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1.getId());
        assertThat(seal.isTapped()).isTrue();

        draw(player1.getId());
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        assertThat(seal.isTapped()).isFalse();

        draw(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    private void draw(java.util.UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
