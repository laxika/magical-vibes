package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerisiaresDevastation.class, GrizzlyBears.class, HillGiant.class, WornPowerstone.class})
class TerisiaresDevastationTest extends BaseCardTest {

    @Test
    @DisplayName("Loses X life, creates X tapped Powerstones, and weakens all creatures by the artifact count")
    void resolvesAllEffects() {
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new WornPowerstone());

        harness.setHand(player1, List.of(new TerisiaresDevastation()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=1: {1}{2}{B}{B}

        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);
        assertThat(findPermanents(player1, "Powerstone").getFirst().isTapped()).isTrue();
        assertThat(ownGiant.getEffectivePower()).isEqualTo(1);
        assertThat(ownGiant.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingGiant.getEffectivePower()).isEqualTo(1);
        assertThat(opposingGiant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The artifact-scaled debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new WornPowerstone());

        harness.setHand(player1, List.of(new TerisiaresDevastation()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=0: {2}{B}{B}
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(ownBear.getEffectivePower()).isEqualTo(1);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
    }
}
