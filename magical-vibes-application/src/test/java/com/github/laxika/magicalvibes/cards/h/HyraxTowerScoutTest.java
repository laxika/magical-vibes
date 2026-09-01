package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HyraxTowerScout.class, GrizzlyBears.class, Island.class})
class HyraxTowerScoutTest extends BaseCardTest {

    @Test
    void entersAndUntapsTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        castScoutTargeting(bears);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void entersAndUntapsTargetCreatureAnOpponentControls() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        castScoutTargeting(bears);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();
        harness.setHand(player1, List.of(new HyraxTowerScout()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castScoutTargeting(Permanent target) {
        harness.setHand(player1, List.of(new HyraxTowerScout()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
