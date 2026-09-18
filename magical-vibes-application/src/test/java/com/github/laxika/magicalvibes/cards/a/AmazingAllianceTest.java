package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmazingAlliance.class, GrizzlyBears.class})
class AmazingAllianceTest extends BaseCardTest {

    @Test
    @DisplayName("Own creatures get +1/+1, but opponents' creatures do not")
    void buffsOnlyOwnCreatures() {
        harness.addToBattlefield(player1, new AmazingAlliance());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains life equal to the number of legendary attackers")
    void gainsLifeForLegendaryAttackers() {
        harness.addToBattlefield(player1, new AmazingAlliance());
        Permanent legendaryBears = addLegendaryBears();
        Permanent ordinaryBears1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ordinaryBears2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        legendaryBears.setSummoningSick(false);
        ordinaryBears1.setSummoningSick(false);
        ordinaryBears2.setSummoningSick(false);
        gd.playerLifeTotals.put(player1.getId(), 20);

        declareAndResolveAttack(List.of(1, 2, 3));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger when only nonlegendary creatures attack")
    void doesNotTriggerWithoutLegendaryAttacker() {
        harness.addToBattlefield(player1, new AmazingAlliance());
        Permanent ordinaryBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ordinaryBears.setSummoningSick(false);
        gd.playerLifeTotals.put(player1.getId(), 20);

        declareAndResolveAttack(List.of(1));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private Permanent addLegendaryBears() {
        GrizzlyBears bears = new GrizzlyBears();
        bears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return harness.addToBattlefieldAndReturn(player1, bears);
    }

    private void declareAndResolveAttack(List<Integer> attackerIndices) {
        declareAttackers(attackerIndices);
        harness.passBothPriorities();
    }
}
