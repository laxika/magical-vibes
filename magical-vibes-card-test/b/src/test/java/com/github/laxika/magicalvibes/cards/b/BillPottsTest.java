package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BillPotts.class, GiantGrowth.class, GrizzlyBears.class, ProdigalPyromancer.class,
        SeedsOfStrength.class})
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

    @Test
    void spellAndAbilityShareTheOncePerTurnLimit() {
        Permanent bill = addCreatureReady(player1, new BillPotts());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bill.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.activateAbility(player1, 1, null, bill.getId());
        resolveAllTriggers();

        assertThat(bill.getPowerModifier()).isEqualTo(6);
        assertThat(bill.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void abilityPreventsAnotherCopyFromASpellThatTurn() {
        Permanent bill = addCreatureReady(player1, new BillPotts());
        addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 1, null, bill.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bill.getId());

        assertThat(bill.getMarkedDamage()).isEqualTo(2);
        assertThat(bill.getPowerModifier()).isEqualTo(3);
    }

    @Test
    void spellCopyCanTargetAnotherCreature() {
        Permanent bill = addCreatureReady(player1, new BillPotts());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bill.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherCreature.getId());
        resolveAllTriggers();

        assertThat(bill.getPowerModifier()).isEqualTo(3);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(3);
    }

    @Test
    void abilityCopyCanTargetAnotherCreature() {
        Permanent bill = addCreatureReady(player1, new BillPotts());
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, bill.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherCreature.getId());
        resolveAllTriggers();

        assertThat(bill.getMarkedDamage()).isEqualTo(1);
        assertThat(otherCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void opponentsSpellDoesNotTriggerBillOrConsumeHisTrigger() {
        Permanent bill = addCreatureReady(player1, new BillPotts());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, bill.getId());

        assertThat(bill.getPowerModifier()).isEqualTo(3);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bill.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(bill.getPowerModifier()).isEqualTo(9);
    }

    @Test
    void triggersWhenOneSpellTargetsBillMultipleTimes() {
        Permanent bill = addCreatureReady(player1, new BillPotts());
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(bill.getId(), bill.getId(), bill.getId()));

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void spellTargetingBillAndAnotherCreatureDoesNotConsumeTheTrigger() {
        Permanent bill = addCreatureReady(player1, new BillPotts());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0,
                List.of(bill.getId(), bill.getId(), otherCreature.getId()));

        assertThat(bill.getPowerModifier()).isEqualTo(2);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bill.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(bill.getPowerModifier()).isEqualTo(8);
    }
}
