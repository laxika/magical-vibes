package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.e.EcologistsTerrarium;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetworkDisruptor.class, EcologistsTerrarium.class, BambooGroveArcher.class, Island.class})
class NetworkDisruptorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target creature")
    void tapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BambooGroveArcher());

        castAndResolve(target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB can tap a noncreature permanent")
    void tapsTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EcologistsTerrarium());

        castAndResolve(target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB can target a permanent you control")
    void tapsTargetPermanentYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EcologistsTerrarium());

        castAndResolve(target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapsTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        castAndResolve(target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetAlreadyTappedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EcologistsTerrarium());
        target.tap();

        castAndResolve(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTapAnotherPermanentWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EcologistsTerrarium());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new Island());
        castWithTarget(target);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(target);

        resolveAllTriggers();

        assertThat(other.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolve(Permanent target) {
        castWithTarget(target);
        resolveAllTriggers();
    }

    private void castWithTarget(Permanent target) {
        harness.setHand(player1, List.of(new NetworkDisruptor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, 0, target.getId());
    }
}
