package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfLimbs.class, AngelOfMercy.class})
class WallOfLimbsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller gains life")
    void putsCounterOnLifeGain() {
        harness.addToBattlefield(player1, new WallOfLimbs());

        Permanent wall = findPermanent(player1, "Wall of Limbs");
        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Angel of Mercy (ETB gain 3 life)
        harness.passBothPriorities(); // resolve GainLifeEffect
        harness.passBothPriorities(); // resolve Wall of Limbs' triggered ability

        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(wall.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when only the opponent gains life")
    void noCounterWhenOpponentGainsLife() {
        harness.addToBattlefield(player1, new WallOfLimbs());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve Angel of Mercy
        harness.passBothPriorities(); // resolve GainLifeEffect

        Permanent wall = findPermanent(player1, "Wall of Limbs");
        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Ability drains life equal to this creature's power")
    void abilityDrainsEqualToPower() {
        harness.addToBattlefield(player1, new WallOfLimbs());
        Permanent wall = findPermanent(player1, "Wall of Limbs");
        wall.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setLife(player2, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ability with no counters causes no life loss")
    void abilityWithZeroPowerDoesNothing() {
        harness.addToBattlefield(player1, new WallOfLimbs());
        harness.setLife(player2, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new WallOfLimbs());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Can target its controller without gaining life")
    void canTargetController() {
        harness.addToBattlefield(player1, new WallOfLimbs());
        findPermanent(player1, "Wall of Limbs").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.assertNotOnBattlefield(player1, "Wall of Limbs");
        harness.assertInGraveyard(player1, "Wall of Limbs");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sacrificing with a pending life-gain trigger uses power before that trigger resolves")
    void sacrificeBeforeCounterTriggerResolves() {
        harness.addToBattlefield(player1, new WallOfLimbs());
        Permanent wall = findPermanent(player1, "Wall of Limbs");
        wall.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 23);

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Wall of Limbs");
        harness.assertInGraveyard(player1, "Wall of Limbs");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 23);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Angel of Mercy");
    }

}
