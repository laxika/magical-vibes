package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KambalProfiteeringMayor;
import com.github.laxika.magicalvibes.cards.t.Treasure;
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

@CardUsed({MrHousePresidentAndCEO.class, Treasure.class, KambalProfiteeringMayor.class})
class MrHousePresidentAndCEOTest extends BaseCardTest {

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
    void resultOfFourCreatesARobot() {
        activateHouse(4);

        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void resultBelowFourCreatesNothing() {
        activateHouse(3);

        assertThat(findPermanents(player1, "Robot")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void resultOfSixCreatesARobotAndATreasure() {
        activateHouse(6);

        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void resultOfFiveCreatesOnlyARobot() {
        activateHouse(5);

        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void resultBelowFourDoesNotTrigger() {
        ReflectionTestUtils.setField(rollD6EffectHandler, "diceRollService", new FixedDiceRollService(3));
        Permanent house = addCreatureReady(player1, new MrHousePresidentAndCEO());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(house), null, null);

        harness.getStackResolutionService().resolveTopOfStack(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    void opponentsRollDoesNotTriggerHouse() {
        addCreatureReady(player2, new MrHousePresidentAndCEO());

        activateHouse(6);

        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Robot")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void robotAndTreasureEnterTogetherForTokenEntryTriggers() {
        harness.addToBattlefield(player1, new KambalProfiteeringMayor());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        activateHouse(6);

        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void treasureManaAddsADieAndEachQualifyingResultTriggers() {
        addTreasure(player1);
        addTreasure(player1);
        activateTreasure("RED");
        activateTreasure("GREEN");

        activateHouseWithTreasureMana(3, 4, 6);

        assertThat(findPermanents(player1, "Robot")).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void activateHouse(int... rolls) {
        activateHouseWithColorlessMana(4, rolls);
    }

    private void activateHouseWithTreasureMana(int... rolls) {
        activateHouseWithColorlessMana(2, rolls);
    }

    private void activateHouseWithColorlessMana(int colorlessMana, int... rolls) {
        ReflectionTestUtils.setField(rollD6EffectHandler, "diceRollService", new FixedDiceRollService(rolls));
        Permanent house = addCreatureReady(player1, new MrHousePresidentAndCEO());
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(house), null, null);
        resolveAllTriggers();
    }

    private void activateTreasure(String color) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int treasureIndex = battlefield.indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, color);
    }

    private void addTreasure(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new Treasure());
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
