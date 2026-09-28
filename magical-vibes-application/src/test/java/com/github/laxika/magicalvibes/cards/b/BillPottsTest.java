package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BillPotts.class, GiantGrowth.class, GrizzlyBears.class, ProdigalPyromancer.class})
class BillPottsTest extends BaseCardTest {

    @Test
    void copiesOnlyTheFirstSingleTargetSpellThatTargetsBillEachTurn() {
        Permanent bill = addCreatureReady(player1, new BillPotts());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bill.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(bill.getPowerModifier()).isEqualTo(6);
        assertThat(bill.getToughnessModifier()).isEqualTo(6);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bill.getId());
        resolveAllTriggers();

        assertThat(bill.getPowerModifier()).isEqualTo(9);
        assertThat(bill.getToughnessModifier()).isEqualTo(9);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, otherCreature.getId());
        resolveAllTriggers();

        assertThat(otherCreature.getPowerModifier()).isEqualTo(3);
    }

    @Test
    void copiesOnlyTheFirstSingleTargetAbilityThatTargetsBillEachTurn() {
        Permanent bill = addCreatureReady(player1, new BillPotts());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 1, null, bill.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        pyromancer.untap();
        harness.activateAbility(player1, 1, null, bill.getId());
        resolveAllTriggers();

        assertThat(bill.getMarkedDamage()).isEqualTo(3);
    }
}
