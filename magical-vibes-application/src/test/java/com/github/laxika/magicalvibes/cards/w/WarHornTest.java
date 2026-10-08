package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ClericOfTheForwardOrder;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarHorn.class, ClericOfTheForwardOrder.class, EnsoulArtifact.class})
class WarHornTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +1/+0")
    void boostsOwnAttackingCreatures() {
        harness.addToBattlefield(player1, new WarHorn());
        Permanent bears = addCreatureReady(player1, new ClericOfTheForwardOrder()); // 2/2

        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-attacking creatures do not get the boost")
    void nonAttackingCreaturesNotBoosted() {
        harness.addToBattlefield(player1, new WarHorn());
        Permanent bears = addCreatureReady(player1, new ClericOfTheForwardOrder()); // 2/2

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's attacking creatures do not get the boost")
    void opponentAttackersNotBoosted() {
        harness.addToBattlefield(player1, new WarHorn());
        Permanent oppBears = addCreatureReady(player2, new ClericOfTheForwardOrder()); // 2/2

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("An animated War Horn boosts itself while attacking")
    void animatedWarHornBoostsItself() {
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new WarHorn());
        horn.setSummoningSick(false);
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, horn.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, horn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, horn)).isEqualTo(5);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gqs.getEffectivePower(gd, horn)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, horn)).isEqualTo(5);
    }

    @Test
    @DisplayName("Multiple War Horns stack only on attacking creatures")
    void multipleHornsBoostOnlyAttackers() {
        harness.addToBattlefield(player1, new WarHorn());
        harness.addToBattlefield(player1, new WarHorn());
        Permanent attacker = addCreatureReady(player1, new ClericOfTheForwardOrder());
        Permanent nonAttacker = addCreatureReady(player1, new ClericOfTheForwardOrder());

        declareAttackersAndPrepareBlockers(List.of(2));

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonAttacker)).isEqualTo(2);
    }
}
