package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiveNoGround;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CursedRonin.class, HumbleBudoka.class, GiveNoGround.class})
class CursedRoninTest extends BaseCardTest {

    @Test
    @DisplayName("Cursed Ronin gets +1/+1 until end of turn when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent ronin = addCreatureReady(player1, new CursedRonin());
        ronin.setAttacking(true);
        addCreatureReady(player2, new HumbleBudoka());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cursed Ronin gets +1/+1 until end of turn when it blocks")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new HumbleBudoka());
        attacker.setAttacking(true);
        Permanent ronin = addCreatureReady(player2, new CursedRonin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cursed Ronin gets only one Bushido bonus when it blocks multiple creatures")
    void blocksMultipleCreaturesGetsOneBushidoBonus() {
        Permanent ronin = addCreatureReady(player2, new CursedRonin());
        addCreatureReady(player1, new CursedRonin());
        addCreatureReady(player1, new CursedRonin());

        harness.setHand(player2, List.of(new GiveNoGround()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, ronin.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(8);
    }

    @Test
    @DisplayName("Paying {B} gives +1/+1 until end of turn, stacking across activations")
    void activatedAbilityPumps() {
        Permanent ronin = addCreatureReady(player1, new CursedRonin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(3);
    }

    @Test
    @DisplayName("The pump from the activated ability wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent ronin = addCreatureReady(player1, new CursedRonin());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(1);
    }
}
