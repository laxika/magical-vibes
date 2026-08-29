package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TomeAnimaTest extends BaseCardTest {

    @Test
    @DisplayName("Tome Anima can't be blocked after its controller draws two cards")
    void cantBeBlockedAfterControllerDrawsTwoCards() {
        Permanent tomeAnima = addCreatureReady(player1, new TomeAnima());
        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isFalse();

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island()));
        draw(player1);
        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isFalse();

        draw(player1);
        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isTrue();
    }

    @Test
    @DisplayName("Tome Anima remains blockable when only an opponent draws two cards")
    void opponentDrawsDoNotEnableUnblockable() {
        Permanent tomeAnima = addCreatureReady(player1, new TomeAnima());

        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Island()));
        draw(player2);
        draw(player2);

        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isFalse();
    }

    private void draw(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
