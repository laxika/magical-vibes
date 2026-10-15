package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BrazenDwarf;
import com.github.laxika.magicalvibes.cards.m.MarneusCalgar;
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

@CardUsed({LuckBobblehead.class, BrazenDwarf.class, MarneusCalgar.class})
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

    @Test
    void sevenSixesAmongMoreThanSevenDiceWins() {
        addBobbleheads(9);
        activateRoll(6, 6, 6, 6, 6, 6, 6, 2, 3);

        assertThat(findPermanents(player1, "Treasure")).hasSize(8);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void opponentsBobbleheadsDoNotIncreaseDiceCount() {
        addBobbleheads(1);
        harness.addToBattlefield(player2, new LuckBobblehead());
        activateRoll(2);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void countsBobbleheadsAtResolution() {
        addBobbleheads(1);
        ReflectionTestUtils.setField(rollD6EffectHandler, "diceRollService", new FixedDiceRollService(2, 4));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player1, new LuckBobblehead());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void abilityResolvesAfterSourceLeavesUsingRemainingBobbleheads() {
        addBobbleheads(2);
        ReflectionTestUtils.setField(rollD6EffectHandler, "diceRollService", new FixedDiceRollService(4));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        Permanent source = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void noBobbleheadsAtResolutionMeansNoDiceOrTreasures() {
        addBobbleheads(1);
        ReflectionTestUtils.setField(rollD6EffectHandler, "diceRollService", new FixedDiceRollService());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        Permanent source = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void sixesFromSeparateResolutionsDoNotAccumulate() {
        addBobbleheads(4);
        activateRoll(6, 6, 6, 6);
        activateRollFrom(1, 6, 6, 6, 1);

        assertThat(findPermanents(player1, "Treasure")).hasSize(7);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void rollingSeveralDiceTriggersBrazenDwarfOnlyOnce() {
        addBobbleheads(3);
        harness.addToBattlefield(player1, new BrazenDwarf());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        activateRoll(1, 3, 5);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void treasuresEnterTogetherAndTriggerMarneusCalgarOnlyOnce() {
        addBobbleheads(3);
        harness.addToBattlefield(player1, new MarneusCalgar());
        harness.setLibrary(player1, List.of(new LuckBobblehead(), new LuckBobblehead(), new LuckBobblehead()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        activateRoll(2, 4, 6);

        assertThat(findPermanents(player1, "Treasure")).hasSize(3).allMatch(Permanent::isTapped);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private void addBobbleheads(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new LuckBobblehead());
        }
    }

    private void activateRoll(int... rolls) {
        activateRollFrom(0, rolls);
    }

    private void activateRollFrom(int permanentIndex, int... rolls) {
        ReflectionTestUtils.setField(rollD6EffectHandler, "diceRollService", new FixedDiceRollService(rolls));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, permanentIndex, 1, null, null);
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
