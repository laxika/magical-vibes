package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({ContactOtherPlane.class, GrizzlyBears.class})
class ContactOtherPlaneTest extends BaseCardTest {

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
    @DisplayName("A result from 1 through 9 draws two cards")
    void lowResultDrawsTwoCards() {
        setRoll(9);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        prepare(List.of(first, second, third));

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("A result from 10 through 19 scries two before drawing two cards")
    void middleResultScriesTwoThenDrawsTwoCards() {
        setRoll(19);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        prepare(List.of(first, second, third, fourth));

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, fourth);
    }

    @Test
    @DisplayName("A result of 20 scries three before drawing three cards")
    void maximumResultScriesThreeThenDrawsThreeCards() {
        setRoll(20);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        prepare(List.of(first, second, third, fourth));

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, second, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    @DisplayName("A result of 1 draws two without scrying")
    void minimumResultDrawsTwoWithoutScrying() {
        setRoll(1);
        Card first = new ContactOtherPlane();
        Card second = new ContactOtherPlane();
        Card third = new ContactOtherPlane();
        prepare(List.of(first, second, third));

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("A result of 10 lets scry bottom a card before drawing")
    void middleRangeStartsAtTenAndDrawsAfterBottoming() {
        setRoll(10);
        Card first = new ContactOtherPlane();
        Card second = new ContactOtherPlane();
        Card third = new ContactOtherPlane();
        Card fourth = new ContactOtherPlane();
        prepare(List.of(first, second, third, fourth));

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, first);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A result of 20 can bottom all three cards before drawing three others")
    void maximumResultCanBottomAllThreeCards() {
        setRoll(20);
        Card first = new ContactOtherPlane();
        Card second = new ContactOtherPlane();
        Card third = new ContactOtherPlane();
        Card fourth = new ContactOtherPlane();
        Card fifth = new ContactOtherPlane();
        Card sixth = new ContactOtherPlane();
        prepare(List.of(first, second, third, fourth, fifth, sixth));

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second, third);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fourth, fifth, sixth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Scry 3 sees only the available cards and still attempts all three draws")
    void maximumResultWithTwoCardsInLibraryLosesOnThirdDraw() {
        setRoll(20);
        Card first = new ContactOtherPlane();
        Card second = new ContactOtherPlane();
        prepare(List.of(first, second));

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
    }

    private void prepare(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
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
