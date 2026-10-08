package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.z.ZombieOgre;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiteDragon.class, ZombieOgre.class})
class WhiteDragonTest extends BaseCardTest {

    @Test
    void entersAndTapsAnOpposingCreatureThatSkipsItsNextUntapStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ZombieOgre());

        castWhiteDragon(bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void cannotTargetACreatureItsControllerControls() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new ZombieOgre()).getId();
        harness.setHand(player1, List.of(new WhiteDragon()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictionExpiresAfterOnlyTheTargetsControllersNextUntapStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZombieOgre());

        castWhiteDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedCreatureStillSkipsItsNextUntapStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZombieOgre());
        target.setTapped(true);

        castWhiteDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    private void castWhiteDragon(UUID targetId) {
        harness.setHand(player1, List.of(new WhiteDragon()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
