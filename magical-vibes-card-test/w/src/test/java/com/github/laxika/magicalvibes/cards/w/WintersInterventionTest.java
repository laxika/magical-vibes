package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WintersIntervention.class, AirElemental.class, GrizzlyBears.class, Forest.class})
class WintersInterventionTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToTargetCreatureAndGainsTwoLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WintersIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    void cannotTargetAPlayerOrNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WintersIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsNoLifeWhenTargetIsIllegalOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WintersIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    void lethalDamageStillGainsLifeAndPutsTheCreatureInItsOwnersGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WintersIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 15);
        harness.setLife(player2, 12);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Winter's Intervention");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 12);
    }

    @Test
    void canTargetOwnCreatureAndGainsLifeWhenAllDamageIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setDamagePreventionShield(2);
        harness.setHand(player1, List.of(new WintersIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 17);
    }
}
