package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({ChaosChanneler.class, HillGiantHerdgorger.class})
class ChaosChannelerTest extends BaseCardTest {

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
    @DisplayName("A result from 1 through 9 exiles the top card")
    void lowRollExilesOneCard() {
        setRoll(9);
        List<Card> library = library(4);

        attackWithChanneler(library);

        assertExiledWithPermission(library.subList(0, 1));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library.subList(1, 4));
    }

    @Test
    @DisplayName("A result from 10 through 19 exiles the top two cards")
    void middleRollExilesTwoCards() {
        setRoll(10);
        List<Card> library = library(4);

        attackWithChanneler(library);

        assertExiledWithPermission(library.subList(0, 2));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library.subList(2, 4));
    }

    @Test
    @DisplayName("A result of 20 exiles the top three cards")
    void maximumRollExilesThreeCards() {
        setRoll(20);
        List<Card> library = library(4);

        attackWithChanneler(library);

        assertExiledWithPermission(library.subList(0, 3));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3));
    }

    @Test
    @DisplayName("A roll of 20 exiles all remaining cards from a short library")
    void shortLibraryExilesOnlyAvailableCards() {
        setRoll(20);
        List<Card> library = library(2);

        attackWithChanneler(library);

        assertExiledWithPermission(library);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library produces no exiled cards or play permissions")
    void emptyLibraryDoesNotExileAnything() {
        setRoll(20);

        attackWithChanneler(List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("An exiled creature can be cast in the postcombat main phase by paying its cost")
    void exiledCreatureCanBeCastWithMana() {
        setRoll(1);
        List<Card> library = library(4);
        attackWithChanneler(library);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromExile(player1, library.getFirst().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant Herdgorger");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(library.getFirst());
    }

    @Test
    @DisplayName("Unplayed cards stay exiled after their play permission expires")
    void unplayedCardsRemainExiledAfterTurnEnds() {
        setRoll(19);
        List<Card> library = library(4);
        attackWithChanneler(library);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library.subList(0, 2));
        for (Card card : library.subList(0, 2)) {
            assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
            assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(card.getId());
        }
    }

    private void attackWithChanneler(List<Card> library) {
        harness.setLibrary(player1, library);
        addCreatureReady(player1, new ChaosChanneler());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
    }

    private void assertExiledWithPermission(List<Card> cards) {
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(cards);
        for (Card card : cards) {
            assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
            assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(card.getId());
            assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(card.getId());
        }
    }

    private List<Card> library(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(ignored -> (Card) new HillGiantHerdgorger())
                .toList();
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
