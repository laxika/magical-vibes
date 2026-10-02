package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieBladecrafter.class, FaerieSeer.class, GrizzlyBears.class, DiabolicEdict.class})
class FaerieBladecrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one +1/+1 counter on itself when one or more Faeries deal combat damage")
    void faerieCombatDamagePutsOneCounter() {
        Permanent bladecrafter = harness.addToBattlefieldAndReturn(player1, new FaerieBladecrafter());
        addAttacker(new FaerieSeer());
        addAttacker(new FaerieSeer());

        runCombatDamage();
        resolveAllTriggers();

        assertThat(bladecrafter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for non-Faerie combat damage")
    void nonFaerieCombatDamageDoesNotTrigger() {
        Permanent bladecrafter = harness.addToBattlefieldAndReturn(player1, new FaerieBladecrafter());
        addAttacker(new GrizzlyBears());

        runCombatDamage();

        assertThat(bladecrafter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("When it dies, each opponent loses life equal to its power and you gain that much life")
    void deathDrainsEachOpponentByPower() {
        Permanent bladecrafter = harness.addToBattlefieldAndReturn(player1, new FaerieBladecrafter());
        bladecrafter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private void addAttacker(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
