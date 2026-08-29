package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderMagic.class, GrizzlyBears.class, HillGiant.class, ColossalDreadmaw.class, Island.class})
class ThunderMagicTest extends BaseCardTest {

    @Test
    @DisplayName("Thunder deals 2 damage to the target creature")
    void thunderDealsTwoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, target, 1);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Thundara deals 4 damage to the target creature")
    void thundaraDealsFourDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(1, target, 4);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Thundaga deals 8 damage to the target creature and pays its red tiered cost")
    void thundagaDealsEightDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        cast(2, target, 7);

        harness.assertNotOnBattlefield(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Thunder Magic cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ThunderMagic()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, Permanent target, int totalMana) {
        harness.setHand(player1, List.of(new ThunderMagic()));
        harness.addMana(player1, ManaColor.RED, mode == 2 ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - (mode == 2 ? 2 : 1));
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
