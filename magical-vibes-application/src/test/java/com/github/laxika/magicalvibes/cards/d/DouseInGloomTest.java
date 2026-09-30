package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GruulNodorog;
import com.github.laxika.magicalvibes.cards.g.GruulTurf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DouseInGloom.class, DaggerclawImp.class, GruulNodorog.class, GruulTurf.class})
class DouseInGloomTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToTargetCreatureAndGainsTwoLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());
        harness.setHand(player1, List.of(new DouseInGloom()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 17);
        harness.assertNotOnBattlefield(player2, "Daggerclaw Imp");
    }

    @Test
    void dealsExactlyTwoDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulNodorog());
        harness.setHand(player1, List.of(new DouseInGloom()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Gruul Nodorog");
        harness.assertLife(player1, 17);
    }

    @Test
    void cannotTargetAPlayerOrNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GruulTurf());
        harness.setHand(player1, List.of(new DouseInGloom()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsNoLifeWhenTargetIsIllegalOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());
        harness.setHand(player1, List.of(new DouseInGloom()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }
}
