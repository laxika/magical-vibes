package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiteDragon.class, GrizzlyBears.class})
class WhiteDragonTest extends BaseCardTest {

    @Test
    void entersAndTapsAnOpposingCreatureThatSkipsItsNextUntapStep() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        castWhiteDragon(bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void cannotTargetACreatureItsControllerControls() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new WhiteDragon()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWhiteDragon(UUID targetId) {
        harness.setHand(player1, List.of(new WhiteDragon()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
