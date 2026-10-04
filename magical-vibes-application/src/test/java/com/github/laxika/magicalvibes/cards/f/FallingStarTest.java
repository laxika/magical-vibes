package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.Tolaria;
import com.github.laxika.magicalvibes.cards.w.WallOfDust;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FallingStar.class, Tolaria.class, WallOfDust.class})
class FallingStarTest extends BaseCardTest {

    @Test
    void doesNotDamageOrTapCreaturesBeforeAFlipOutcomeIsSupplied() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WallOfDust());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WallOfDust());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Tolaria());

        harness.castFromHand(player1, new FallingStar(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }
}
