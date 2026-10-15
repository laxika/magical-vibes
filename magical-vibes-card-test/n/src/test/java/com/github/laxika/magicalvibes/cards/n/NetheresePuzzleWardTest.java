package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ContactOtherPlane;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.D4RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD4EffectHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetheresePuzzleWard.class, ContactOtherPlane.class})
class NetheresePuzzleWardTest extends BaseCardTest {

    private RollD4EffectHandler rollD4EffectHandler;
    private RollD20EffectHandler rollD20EffectHandler;
    private D4RollService originalD4RollService;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureRollServices() {
        rollD4EffectHandler = GameTestEngineContext.get().getBean(RollD4EffectHandler.class);
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD4RollService = (D4RollService) ReflectionTestUtils.getField(
                rollD4EffectHandler, "d4RollService");
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
    }

    @AfterEach
    void restoreRollServices() {
        ReflectionTestUtils.setField(rollD4EffectHandler, "d4RollService", originalD4RollService);
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    void naturalFourScriesFourThenDraws() {
        setD4Roll(4);
        gd.playerHands.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new NetheresePuzzleWard());
        harness.setLibrary(player1, libraryCards(5));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        answerScry(4);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void nonMaximumD4StillScriesButDoesNotDraw() {
        setD4Roll(3);
        gd.playerHands.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new NetheresePuzzleWard());
        harness.setLibrary(player1, libraryCards(4));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(3);
        answerScry(3);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void naturalTwentyAlsoDrawsFromAnotherDie() {
        setD20Roll(20);
        harness.addToBattlefield(player1, new NetheresePuzzleWard());
        harness.setLibrary(player1, libraryCards(8));
        harness.castFromHand(player1, new ContactOtherPlane(), "{3}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(3);
        answerScry(3);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void opponentsUpkeepDoesNotRollOrScry() {
        setD4Roll(4);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new NetheresePuzzleWard());
        harness.setLibrary(player1, libraryCards(5));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    void opponentsHighestRollDoesNotDrawForWardController() {
        setD20Roll(20);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new NetheresePuzzleWard());
        harness.setLibrary(player1, libraryCards(5));
        harness.setLibrary(player2, libraryCards(5));

        harness.castFromHand(player2, new ContactOtherPlane(), "{3}{U}");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(3);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    void eachWardDrawsForTheSameHighestRoll() {
        setD20Roll(20);
        harness.addToBattlefield(player1, new NetheresePuzzleWard());
        harness.addToBattlefield(player1, new NetheresePuzzleWard());
        harness.setLibrary(player1, libraryCards(8));

        harness.castFromHand(player1, new ContactOtherPlane(), "{3}{U}");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(3);
        answerScry(3);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }

    private List<Card> libraryCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .<Card>mapToObj(ignored -> new NetheresePuzzleWard())
                .toList();
    }

    private void answerScry(int count) {
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(
                        java.util.stream.IntStream.range(0, count).boxed().toList(), List.of()));
    }

    private void setD4Roll(int result) {
        ReflectionTestUtils.setField(rollD4EffectHandler, "d4RollService", new FixedD4RollService(result));
    }

    private void setD20Roll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
    }

    private static final class FixedD4RollService extends D4RollService {

        private final int result;

        private FixedD4RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
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
