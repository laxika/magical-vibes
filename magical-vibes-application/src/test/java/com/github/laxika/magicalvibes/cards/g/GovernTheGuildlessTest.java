package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.SimicSignet;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({GovernTheGuildless.class, AssaultZeppelid.class, MistralCharger.class, SimicSignet.class})
class GovernTheGuildlessTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of a target monocolored creature")
    void gainsControlOfMonocoloredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        harness.setHand(player1, List.of(new GovernTheGuildless()));
        addSpellMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Mistral Charger");
        harness.assertNotOnBattlefield(player2, "Mistral Charger");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with the control spell")
    void controlSpellRejectsNoncreature() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new SimicSignet());
        harness.setHand(player1, List.of(new GovernTheGuildless()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("monocolored creature");
    }

    @Test
    @DisplayName("Cannot target a multicolored creature with the control spell")
    void controlSpellRejectsMulticoloredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new GovernTheGuildless()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("monocolored creature");
    }

    @Test
    @DisplayName("Forecast lets a creature become multiple chosen colors until end of turn")
    void forecastChangesCreatureColors() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        GovernTheGuildless card = new GovernTheGuildless();
        harness.setHand(player1, List.of(card));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, target))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Forecast can be activated only once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        harness.setHand(player1, List.of(new GovernTheGuildless()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, firstTarget.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast can be activated only during its controller's upkeep")
    void forecastRequiresControllerUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        harness.setHand(player1, List.of(new GovernTheGuildless()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only be activated during your upkeep");
    }

    @Test
    @DisplayName("Forecast replaces multiple colors and enables permanent control")
    void forecastMakesMulticoloredCreatureMonocolored() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new GovernTheGuildless()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleListChoice(player1, "DONE"));

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        addSpellMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.assertOnBattlefield(player1, "Assault Zeppelid");

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveColors(gd, target))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        harness.assertOnBattlefield(player1, "Assault Zeppelid");
        harness.assertNotOnBattlefield(player2, "Assault Zeppelid");
    }

    @Test
    @DisplayName("Each separate copy can forecast during the same upkeep")
    void separateCopiesCanForecast() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        harness.setHand(player1, List.of(new GovernTheGuildless(), new GovernTheGuildless()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleListChoice(player1, "DONE"));
        harness.activateHandAbility(player1, 1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleListChoice(player1, "DONE"));

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Forecast cannot be activated during an opponent's upkeep")
    void forecastRejectsOpponentUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        harness.setHand(player1, List.of(new GovernTheGuildless()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only be activated during your upkeep");
    }

    @Test
    @DisplayName("Forecast keeps its source revealed in hand for the rest of the upkeep")
    void forecastSourceRemainsRevealedUntilUpkeepEnds() throws Exception {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        GovernTheGuildless source = new GovernTheGuildless();
        harness.setHand(player1, List.of(source, new SimicSignet()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleListChoice(player1, "DONE"));
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

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
