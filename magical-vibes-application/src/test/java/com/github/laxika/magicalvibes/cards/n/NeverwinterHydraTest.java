package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.effect.entryfx.RollDiceAndEnterWithCountersEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeverwinterHydra.class, Shock.class})
class NeverwinterHydraTest extends BaseCardTest {

    private RollDiceAndEnterWithCountersEffectHandler handler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        handler = GameTestEngineContext.get().getBean(RollDiceAndEnterWithCountersEffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(handler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(handler, "diceRollService", originalDiceRollService);
    }

    @Test
    void entersWithCountersEqualToTheTotalOfTheRolls() {
        ReflectionTestUtils.setField(handler, "diceRollService", new FixedDiceRollService(1, 4, 6));
        NeverwinterHydra hydra = new NeverwinterHydra();
        harness.setHand(player1, List.of(hydra));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Neverwinter Hydra");
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(11);
        assertThat(permanent.getEffectivePower()).isEqualTo(11);
        assertThat(permanent.getEffectiveToughness()).isEqualTo(11);
        assertThat(gameLogContains("rolls 3 d6 for Neverwinter Hydra: [1, 4, 6].")).isTrue();
    }

    @Test
    void wardCountersAnOpponentSpellWhenTheyDoNotPay() {
        ReflectionTestUtils.setField(handler, "diceRollService", new FixedDiceRollService(1));
        NeverwinterHydra hydra = new NeverwinterHydra();
        harness.setHand(player1, List.of(hydra));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Neverwinter Hydra");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, permanent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Neverwinter Hydra");
    }

    @Test
    void zeroXRollsNoDiceAndDiesWithoutCounters() {
        ReflectionTestUtils.setField(handler, "diceRollService", new FixedDiceRollService());
        harness.setHand(player1, List.of(new NeverwinterHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Neverwinter Hydra");
        harness.assertInGraveyard(player1, "Neverwinter Hydra");
        assertThat(gameLogContains("rolls 0 d6")).isFalse();
    }

    @Test
    void wardDoesNotTriggerForItsControllersSpell() {
        ReflectionTestUtils.setField(handler, "diceRollService", new FixedDiceRollService(1));
        harness.setHand(player1, List.of(new NeverwinterHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Neverwinter Hydra");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, permanent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Neverwinter Hydra");
        harness.assertNotOnBattlefield(player1, "Neverwinter Hydra");
    }

    @Test
    void opponentCanPayFourToLetTheirSpellResolve() {
        castHydraAndTargetWithShockWithWardMana();

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Neverwinter Hydra");
        harness.assertNotOnBattlefield(player1, "Neverwinter Hydra");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void opponentCanDeclineWardEvenWhenTheyHaveEnoughMana() {
        castHydraAndTargetWithShockWithWardMana();

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Neverwinter Hydra");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    void trampleDealsDamageBeyondALethallyDamagedBlocker() {
        Permanent attacker = addCreatureReady(player1, new NeverwinterHydra());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        Permanent blocker = addCreatureReady(player2, new NeverwinterHydra());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Neverwinter Hydra");
        harness.assertOnBattlefield(player1, "Neverwinter Hydra");
    }

    private void castHydraAndTargetWithShockWithWardMana() {
        ReflectionTestUtils.setField(handler, "diceRollService", new FixedDiceRollService(1));
        harness.setHand(player1, List.of(new NeverwinterHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Neverwinter Hydra");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castInstant(player2, 0, permanent.getId());
        harness.passBothPriorities();
    }
    private static final class FixedDiceRollService extends DiceRollService {

        private final int[] results;
        private int index;

        private FixedDiceRollService(int... results) {
            this.results = results;
        }

        @Override
        public int roll(int sides) {
            return results[index++];
        }
    }
}
