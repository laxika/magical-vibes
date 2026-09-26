package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValiantVeteran.class, YotianSoldier.class, GrizzlyBears.class})
class ValiantVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Other Soldiers you control get +1/+1")
    void boostsOtherSoldiersYouControl() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent nonSoldier = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentSoldier = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());

        int soldierPower = gqs.getEffectivePower(gd, soldier);
        int soldierToughness = gqs.getEffectiveToughness(gd, soldier);
        harness.addToBattlefield(player1, new ValiantVeteran());

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(soldierToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonSoldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSoldier)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(soldierPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(soldierToughness);
    }

    @Test
    @DisplayName("Graveyard ability exiles Valiant Veteran and puts counters on own Soldiers")
    void graveyardAbilityExilesAndCountersOwnSoldiers() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent nonSoldier = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentSoldier = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        Card veteran = new ValiantVeteran();
        harness.setGraveyard(player1, List.of(veteran));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Valiant Veteran");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(veteran);

        harness.passBothPriorities();

        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonSoldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentSoldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
