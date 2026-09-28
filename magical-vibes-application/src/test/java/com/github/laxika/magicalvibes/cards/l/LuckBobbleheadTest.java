package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD6EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LuckBobblehead.class)
class LuckBobbleheadTest extends BaseCardTest {

    private RollD6EffectHandler rollD6EffectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        rollD6EffectHandler = GameTestEngineContext.get().getBean(RollD6EffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(
                rollD6EffectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(rollD6EffectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    void manaAbilityAddsChosenColor() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new LuckBobblehead());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bobblehead.isTapped()).isTrue();
    }

    @Test
    void createsTappedTreasureForEachEvenResult() {
        addBobbleheads(6);
        activateRoll(1, 2, 3, 4, 5, 6);

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(3);
        assertThat(treasures).allMatch(Permanent::isTapped);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void exactlySevenSixesWinsTheGame() {
        addBobbleheads(7);
        activateRoll(6, 6, 6, 6, 6, 6, 6);

        assertThat(findPermanents(player1, "Treasure")).hasSize(7);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void moreThanSevenSixesDoesNotWin() {
        addBobbleheads(8);
        activateRoll(6, 6, 6, 6, 6, 6, 6, 6);

        assertThat(findPermanents(player1, "Treasure")).hasSize(8);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.winnerPlayerId).isNull();
    }

    private void addBobbleheads(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new LuckBobblehead());
        }
    }

    private void activateRoll(int... rolls) {
        ReflectionTestUtils.setField(rollD6EffectHandler, "diceRollService", new FixedDiceRollService(rolls));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();
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
