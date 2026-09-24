package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetworkDisruptor.class, FountainOfYouth.class, GrizzlyBears.class})
class NetworkDisruptorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target creature")
    void tapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB can tap a noncreature permanent")
    void tapsTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castAndResolve(target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB can target a permanent you control")
    void tapsTargetPermanentYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        castAndResolve(target);

        assertThat(target.isTapped()).isTrue();
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new NetworkDisruptor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
