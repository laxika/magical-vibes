package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ContactOtherPlane;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrazenDwarf.class, ContactOtherPlane.class})
class BrazenDwarfTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(9));
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    void dealsDamageToEachOpponentWhenControllerRollsADie() {
        harness.addToBattlefield(player1, new BrazenDwarf());
        harness.setLibrary(player1, List.of(new BrazenDwarf(), new BrazenDwarf()));
        harness.setHand(player1, List.of(new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    void doesNotTriggerWhenOpponentRollsADie() {
        harness.addToBattlefield(player1, new BrazenDwarf());
        harness.setLibrary(player2, List.of(new BrazenDwarf(), new BrazenDwarf()));
        harness.setHand(player2, List.of(new ContactOtherPlane()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, controllerLifeBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    void triggersAgainForASeparateRollInTheSameTurn() {
        harness.addToBattlefield(player1, new BrazenDwarf());
        harness.setLibrary(player1, List.of(new BrazenDwarf(), new BrazenDwarf(),
                new BrazenDwarf(), new BrazenDwarf()));
        harness.setHand(player1, List.of(new ContactOtherPlane(), new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLifeBefore);
        harness.assertLife(player2, opponentLifeBefore - 2);
    }

    @Test
    void damageWaitsForTriggerResolutionAndSurvivesSourceLeavingBattlefield() {
        harness.addToBattlefield(player1, new BrazenDwarf());
        harness.setLibrary(player1, List.of(new BrazenDwarf(), new BrazenDwarf()));
        harness.setHand(player1, List.of(new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, opponentLifeBefore);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, controllerLifeBefore);
        harness.assertLife(player2, opponentLifeBefore - 1);
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int result;

        private FixedD20RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
