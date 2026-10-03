package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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

@CardUsed({DjinniWindseer.class, HillGiantHerdgorger.class})
class DjinniWindseerTest extends BaseCardTest {

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
    @DisplayName("A result of 9 scries 1")
    void nineScriesOne() {
        setRoll(9);
        Card first = new HillGiantHerdgorger();
        Card second = new HillGiantHerdgorger();
        harness.setLibrary(player1, List.of(first, second));

        castDjinniWindseer();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.cards()).containsExactly(first);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("A result of 10 scries 2")
    void tenScriesTwo() {
        setRoll(10);
        prepareLibrary(3);

        castDjinniWindseer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(2);
    }

    @Test
    @DisplayName("A result of 19 scries 2")
    void nineteenScriesTwo() {
        setRoll(19);
        prepareLibrary(3);

        castDjinniWindseer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(2);
    }

    @Test
    @DisplayName("A result of 20 scries 3")
    void twentyScriesThree() {
        setRoll(20);
        prepareLibrary(4);

        castDjinniWindseer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(3);
    }

    @Test
    @DisplayName("A result of 1 scries 1 and can leave the card on top")
    void oneScriesOneAndKeepsTopCard() {
        setRoll(1);
        Card first = new HillGiantHerdgorger();
        Card second = new HillGiantHerdgorger();
        harness.setLibrary(player1, List.of(first, second));

        castDjinniWindseer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Scry 3 permits ordering cards above and below the untouched library")
    void scryThreeOrdersTopAndBottomCards() {
        setRoll(20);
        Card first = new HillGiantHerdgorger();
        Card second = new HillGiantHerdgorger();
        Card third = new HillGiantHerdgorger();
        Card fourth = new HillGiantHerdgorger();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castDjinniWindseer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(2, 0), List.of(1)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, fourth, second);
    }

    @Test
    @DisplayName("Scry 3 looks at all available cards when the library has fewer than three")
    void scryThreeWithShortLibrary() {
        setRoll(20);
        Card onlyCard = new HillGiantHerdgorger();
        harness.setLibrary(player1, List.of(onlyCard));

        castDjinniWindseer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    @DisplayName("Scry with an empty library completes without requiring a choice")
    void scryWithEmptyLibrary() {
        setRoll(20);
        harness.setLibrary(player1, List.of());

        castDjinniWindseer();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Djinni Windseer");
    }

    @Test
    @DisplayName("The entering creature's controller scries their own library after the trigger resolves")
    void opponentControllerScriesOwnLibrary() {
        setRoll(10);
        Card ownCard = new HillGiantHerdgorger();
        Card first = new HillGiantHerdgorger();
        Card second = new HillGiantHerdgorger();
        Card third = new HillGiantHerdgorger();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(first, second, third));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new DjinniWindseer(), "{3}{U}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Djinni Windseer");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second, third);

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, third, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
    }

    private void prepareLibrary(int size) {
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, size)
                .mapToObj(ignored -> (Card) new HillGiantHerdgorger())
                .toList());
    }

    private void castDjinniWindseer() {
        harness.castFromHand(player1, new DjinniWindseer(), "{3}{U}");
        resolveAllTriggers();
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
