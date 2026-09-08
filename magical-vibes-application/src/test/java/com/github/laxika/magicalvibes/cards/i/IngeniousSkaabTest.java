package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IngeniousSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess gives Ingenious Skaab +1/+1 after casting a noncreature spell")
    void prowessBoostsAfterCastingNoncreatureSpell() {
        Permanent skaab = addReadySkaab();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(skaab.getPowerModifier()).isEqualTo(1);
        assertThat(skaab.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prowess does not trigger from casting a creature spell")
    void prowessDoesNotTriggerForCreatureSpell() {
        Permanent skaab = addReadySkaab();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(skaab.getPowerModifier()).isEqualTo(0);
        assertThat(skaab.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The blue ability gives Ingenious Skaab +1/-1 until end of turn")
    void blueAbilityBoostsUntilEndOfTurn() {
        Permanent skaab = addReadySkaab();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skaab.getPowerModifier()).isEqualTo(1);
        assertThat(skaab.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skaab.getPowerModifier()).isEqualTo(0);
        assertThat(skaab.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadySkaab() {
        Permanent skaab = new Permanent(new IngeniousSkaab());
        skaab.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(skaab);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return skaab;
    }
}
