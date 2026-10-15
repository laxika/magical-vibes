package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikedPitTrap.class, HillGiantHerdgorger.class, Forest.class})
class SpikedPitTrapTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    @DisplayName("A result from 1 through 9 deals 5 damage without creating a Treasure")
    void lowRollDealsDamageWithoutTreasure() {
        setRoll(9);
        Permanent target = activateAgainstCreature();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Spiked Pit Trap");
    }

    @Test
    @DisplayName("A result from 10 through 20 deals 5 damage and creates a Treasure")
    void highRollDealsDamageAndCreatesTreasure() {
        setRoll(10);
        Permanent target = activateAgainstCreature();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInGraveyard(player1, "Spiked Pit Trap");
    }

    @Test
    @DisplayName("The ability can target only a creature")
    void cannotTargetNonCreaturePermanent() {
        Permanent trap = harness.addToBattlefieldAndReturn(player1, new SpikedPitTrap());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(trap);
    }

    @Test
    void naturalOneDealsDamageWithoutTreasure() {
        setRoll(1);
        Permanent target = activateAgainstCreature();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void naturalTwentyCreatesUntappedTreasureForController() {
        setRoll(20);
        Permanent target = activateAgainstCreature();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void canCastWithFlashDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SpikedPitTrap()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.passPriority(gd, player2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spiked Pit Trap");
        harness.assertNotInHand(player1, "Spiked Pit Trap");
    }

    @Test
    void sacrificeIsPaidBeforeAbilityResolves() {
        setRoll(10);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.addToBattlefield(player1, new SpikedPitTrap());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Spiked Pit Trap");
        harness.assertInGraveyard(player1, "Spiked Pit Trap");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void cannotActivateTappedTrap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent trap = harness.addToBattlefieldAndReturn(player1, new SpikedPitTrap());
        trap.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spiked Pit Trap");
        harness.assertNotInGraveyard(player1, "Spiked Pit Trap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyFourMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent trap = harness.addToBattlefieldAndReturn(player1, new SpikedPitTrap());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(trap.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Spiked Pit Trap");
        harness.assertNotInGraveyard(player1, "Spiked Pit Trap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void missingTargetPreventsRollAndTreasure() {
        FixedD20RollService dice = new FixedD20RollService(20);
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", dice);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.addToBattlefield(player1, new SpikedPitTrap());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(dice.rollCount).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Spiked Pit Trap");
        assertThat(gd.stack).isEmpty();
    }
    private Permanent activateAgainstCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.addToBattlefield(player1, new SpikedPitTrap());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        return target;
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int result;
        private int rollCount;

        private FixedD20RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            rollCount++;
            return result;
        }
    }
}
