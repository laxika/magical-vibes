package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderManHometownHero.class, GrizzlyBears.class, HillGiant.class})
class SpiderManHometownHeroTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes a target creature with power 2 or less unblockable this turn")
    void etbMakesLowPowerCreatureUnblockable() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castSpiderMan(bears);

        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A creature with power 3 is not a legal target")
    void cannotTargetHighPowerCreature() {
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpiderManHometownHero()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(hillGiant.isCantBeBlocked()).isFalse();
    }

    private void castSpiderMan(Permanent target) {
        harness.setHand(player1, List.of(new SpiderManHometownHero()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
