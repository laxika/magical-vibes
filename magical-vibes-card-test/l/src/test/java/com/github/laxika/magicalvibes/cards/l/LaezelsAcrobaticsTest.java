package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaezelsAcrobatics.class, ElvishVisionary.class, Forest.class, GrizzlyBears.class})
class LaezelsAcrobaticsTest extends BaseCardTest {

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
    void lowRollReturnsNontokenCreaturesAtNextEndStep() {
        setRoll(1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAcrobatics();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void highRollReturnsCreaturesThenExilesThemAgainBeforeNextEndStep() {
        setRoll(10);
        harness.addToBattlefield(player1, new ElvishVisionary());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        castAcrobatics();

        harness.assertNotOnBattlefield(player1, "Elvish Visionary");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Elvish Visionary");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof Forest)
                .hasSize(1);

        advanceToEndStep();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof Forest)
                .hasSize(2);
        harness.assertOnBattlefield(player1, "Elvish Visionary");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 9, 10, 20})
    void creaturesAreAlreadyExiledWhenTheDieIsRolled(int result) {
        harness.addToBattlefield(player1, new GrizzlyBears());
        boolean[] rolled = {false};
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new D20RollService() {
            @Override
            public int roll() {
                rolled[0] = true;
                harness.assertNotOnBattlefield(player1, "Grizzly Bears");
                assertThat(gd.getPlayerExiledCards(player1.getId()))
                        .extracting(card -> card.getName())
                        .containsExactly("Grizzly Bears");
                return result;
            }
        });

        castAcrobatics();

        assertThat(rolled[0]).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {9, 20})
    void creatureTokensRemainOnTheBattlefield(int result) {
        setRoll(result);
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        var token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAcrobatics();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(token);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature.getCard());
        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 20})
    void borrowedCreaturesReturnToTheirOwnerAfterBothRollOutcomes(int result) {
        setRoll(result);
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(creature.getId(), player2.getId());

        castAcrobatics();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature.getCard());
        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castAcrobatics() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LaezelsAcrobatics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
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
