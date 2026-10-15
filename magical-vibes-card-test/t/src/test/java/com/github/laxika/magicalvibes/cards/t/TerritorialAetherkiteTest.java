package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerritorialAetherkite.class, GrizzlyBears.class})
class TerritorialAetherkiteTest extends BaseCardTest {

    @Test
    void gainsEnergyAndDealsPaidDamageToEachOtherCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aetherkite = cast();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        PendingInteraction.XValueChoice choice = gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 1);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(aetherkite.getMarkedDamage()).isZero();
    }

    @Test
    void decliningPaymentDealsNoDamage() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        cast();

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    private Permanent cast() {
        harness.setHand(player1, List.of(new TerritorialAetherkite()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TerritorialAetherkite)
                .findFirst()
                .orElseThrow();
    }
}
