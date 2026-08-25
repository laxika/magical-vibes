package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaughtInTheCrossfire.class, DeathcultRogue.class, HillGiant.class})
class CaughtInTheCrossfireTest extends BaseCardTest {

    @Test
    @DisplayName("The outlaw mode damages outlaw creatures on both battlefields only")
    void damagesOutlawCreaturesOnly() {
        addCreatureReady(player1, new DeathcultRogue());
        addCreatureReady(player2, new DeathcultRogue());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new HillGiant());

        cast(new int[]{0}, 3);

        harness.assertNotOnBattlefield(player1, "Deathcult Rogue");
        harness.assertNotOnBattlefield(player2, "Deathcult Rogue");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The non-outlaw mode damages non-outlaw creatures only")
    void damagesNonOutlawCreaturesOnly() {
        addCreatureReady(player1, new DeathcultRogue());
        addCreatureReady(player2, new DeathcultRogue());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new HillGiant());

        cast(new int[]{1}, 3);

        harness.assertOnBattlefield(player1, "Deathcult Rogue");
        harness.assertOnBattlefield(player2, "Deathcult Rogue");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(findPermanent(player1, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Spree resolves both modes and charges both additional costs")
    void resolvesBothModes() {
        addCreatureReady(player1, new DeathcultRogue());
        addCreatureReady(player2, new DeathcultRogue());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new HillGiant());

        cast(new int[]{0, 1}, 4);

        harness.assertNotOnBattlefield(player1, "Deathcult Rogue");
        harness.assertNotOnBattlefield(player2, "Deathcult Rogue");
        assertThat(findPermanent(player1, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    private void cast(int[] modes, int redMana) {
        harness.setHand(player1, List.of(new CaughtInTheCrossfire()));
        harness.addMana(player1, ManaColor.RED, redMana);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, List.of());
        harness.passBothPriorities();
    }

}
