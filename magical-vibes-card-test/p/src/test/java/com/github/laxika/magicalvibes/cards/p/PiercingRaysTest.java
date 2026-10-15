package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DreyKeeper;
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

@CardUsed({PiercingRays.class, DreyKeeper.class})
class PiercingRaysTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target tapped creature")
    void exilesTargetTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        target.tap();
        harness.setHand(player1, List.of(new PiercingRays()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getId())
                .contains(target.getCard().getId());
    }

    @Test
    @DisplayName("Cannot cast on an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        harness.setHand(player1, List.of(new PiercingRays()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Forecast taps an untapped creature and keeps Piercing Rays in hand")
    void forecastTapsUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        PiercingRays rays = new PiercingRays();
        harness.setHand(player1, List.of(rays));
        prepareUpkeepAndMana();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Piercing Rays");
    }

    @Test
    @DisplayName("Forecast requires an untapped creature")
    void forecastCannotTargetTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        target.tap();
        harness.setHand(player1, List.of(new PiercingRays()));
        prepareUpkeepAndMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forecast can be activated only once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        harness.setHand(player1, List.of(new PiercingRays()));
        prepareUpkeepAndMana();

        harness.activateHandAbility(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast cannot be activated outside its controller's upkeep")
    void forecastRequiresUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        harness.setHand(player1, List.of(new PiercingRays()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");
    }

    @Test
    @DisplayName("An untapped target is not exiled when the spell resolves")
    void untappedTargetIsIllegalOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        target.tap();
        harness.setHand(player1, List.of(new PiercingRays()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Drey Keeper");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Piercing Rays");
    }

    @Test
    @DisplayName("Forecast cannot be activated during the opponent's upkeep")
    void forecastRejectsOpponentsUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        harness.setHand(player1, List.of(new PiercingRays()));
        prepareUpkeepAndMana();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");
    }

    @Test
    @DisplayName("Each copy can forecast once in the same upkeep")
    void separateCopiesCanForecast() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        harness.setHand(player1, List.of(new PiercingRays(), new PiercingRays()));
        prepareUpkeepAndMana();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, first.getId());
        harness.activateHandAbility(player1, 1, second.getId());
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Forecast keeps only its source revealed until the upkeep ends")
    void forecastKeepsSourceRevealedDuringUpkeep() throws Exception {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreyKeeper());
        PiercingRays source = new PiercingRays();
        harness.setHand(player1, List.of(source, new DreyKeeper()));
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
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
