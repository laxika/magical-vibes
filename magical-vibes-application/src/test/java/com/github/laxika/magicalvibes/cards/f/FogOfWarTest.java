package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FogOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each creature on the battlefield")
    void gainsLifeForEachCreatureOnBattlefield() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castFogOfWar();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Prevents combat damage from creatures with power 3 or less")
    void preventsCombatDamageFromCreaturesWithPowerThreeOrLess() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castFogOfWar();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, giant, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, elemental, true)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, giant, false)).isFalse();
    }

    @Test
    @DisplayName("Combat damage prevention wears off at end of turn")
    void combatDamagePreventionWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFogOfWar();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isFalse();
    }

    private void castFogOfWar() {
        harness.setHand(player1, List.of(new FogOfWar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
