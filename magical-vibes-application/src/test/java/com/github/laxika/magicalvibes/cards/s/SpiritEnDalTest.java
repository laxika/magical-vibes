package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritEnDal.class, BladeOfTheSixthPride.class})
class SpiritEnDalTest extends BaseCardTest {

    @Test
    @DisplayName("Forecast grants shadow to a target creature and keeps Spirit en-Dal in hand")
    void grantsShadowFromHand() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        prepareUpkeepAndMana();

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHADOW)).isTrue();
        harness.assertInHand(player1, "Spirit en-Dal");
    }

    @Test
    @DisplayName("Forecast can be activated only once each turn")
    void forecastOnlyOnceEachTurn() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        prepareUpkeepAndMana();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, bears.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast is restricted to its controller's upkeep")
    void forecastOnlyDuringYourUpkeep() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This ability can only be activated during your upkeep");
        harness.assertInHand(player1, "Spirit en-Dal");
    }

    @Test
    @DisplayName("Forecast's shadow wears off at end of turn")
    void shadowWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        prepareUpkeepAndMana();

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHADOW)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Forecast targets a creature, not a player")
    void forecastCannotTargetPlayer() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        prepareUpkeepAndMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each copy can forecast once during the same upkeep")
    void separateCopiesCanForecast() {
        harness.setHand(player1, List.of(new SpiritEnDal(), new SpiritEnDal()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSixthPride());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        prepareUpkeepAndMana();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, first.getId());
        harness.activateHandAbility(player1, 1, second.getId());
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        assertThat(gqs.hasKeyword(gd, first, Keyword.SHADOW)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.SHADOW)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Forecast cannot be activated during its owner's main phase")
    void forecastCannotBeActivatedInMainPhase() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSixthPride());
        prepareUpkeepAndMana();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This ability can only be activated during your upkeep");
        harness.assertInHand(player1, "Spirit en-Dal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forecast requires both the generic and white mana payments")
    void insufficientManaDoesNotConsumeForecastUse() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSixthPride());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Spirit en-Dal");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Forecast keeps only its source revealed until upkeep ends")
    void forecastKeepsSourceRevealedUntilUpkeepEnds() throws Exception {
        SpiritEnDal source = new SpiritEnDal();
        harness.setHand(player1, List.of(source, new SpiritEnDal()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        prepareUpkeepAndMana();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());
        harness.publishState();

        String message = harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
        GameStateMessage state = new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class);
        assertThat(state.opponentHand()).extracting(card -> card.id()).containsExactly(source.getId());

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.publishState();
        message = harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
        state = new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class);
        assertThat(state.opponentHand()).isEmpty();
    }

    private void prepareUpkeepAndMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
