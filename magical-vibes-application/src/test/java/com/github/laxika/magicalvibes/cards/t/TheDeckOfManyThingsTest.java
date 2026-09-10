package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({TheDeckOfManyThings.class, GrizzlyBears.class})
class TheDeckOfManyThingsTest extends BaseCardTest {

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
    void subtractsHandSizeAndReturnsRandomCardOnResultFromOneThroughNine() {
        setRoll(9);
        Card returned = new GrizzlyBears();
        harness.addToBattlefield(player1, new TheDeckOfManyThings());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(returned));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(returned);
    }

    @Test
    void drawsTwoCardsOnResultFromTenThroughNineteen() {
        setRoll(10);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.addToBattlefield(player1, new TheDeckOfManyThings());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void discardsHandWhenAdjustedResultIsZeroOrLess() {
        setRoll(2);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.addToBattlefield(player1, new TheDeckOfManyThings());
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void returnsCreatureFromAnyGraveyardAndItsOwnerLosesWhenItDies() {
        setRoll(20);
        Card returnedCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new TheDeckOfManyThings());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player2, List.of(returnedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == returnedCard)
                .findFirst()
                .orElseThrow();
        returned.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
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
