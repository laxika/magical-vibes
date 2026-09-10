package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.t.TrumpetingArmodon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeveredConvulsions.class, LotusPetal.class, TrumpetingArmodon.class})
class FeveredConvulsionsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability puts a -1/-1 counter on target creature")
    void abilityPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new FeveredConvulsions());
        Permanent armodon = harness.addToBattlefieldAndReturn(player2, new TrumpetingArmodon());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.passBothPriorities();

        assertThat(armodon.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(armodon.getEffectivePower()).isEqualTo(2);
        assertThat(armodon.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly to shrink a creature further")
    void abilityStacksCounters() {
        harness.addToBattlefield(player1, new FeveredConvulsions());
        Permanent armodon = harness.addToBattlefieldAndReturn(player2, new TrumpetingArmodon());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 12);

        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Trumpeting Armodon");
        harness.assertInGraveyard(player2, "Trumpeting Armodon");
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new FeveredConvulsions());
        Permanent lotusPetal = harness.addToBattlefieldAndReturn(player2, new LotusPetal());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, lotusPetal.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Ability requires two black mana in addition to its generic cost")
    void requiresTwoBlackMana() {
        harness.addToBattlefield(player1, new FeveredConvulsions());
        Permanent armodon = harness.addToBattlefieldAndReturn(player2, new TrumpetingArmodon());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, armodon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }
}
