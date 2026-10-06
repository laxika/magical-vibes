package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.g.GnatAlleyCreeper;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
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

@CardUsed({SkyHussar.class, MistralCharger.class, SilkwingScout.class, GnatAlleyCreeper.class, AzoriusSignet.class})
class SkyHussarTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, untaps all creatures its controller controls")
    void entersAndUntapsControlledCreaturesOnly() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new SilkwingScout());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GnatAlleyCreeper());
        whiteCreature.tap();
        blueCreature.tap();
        opponentCreature.tap();

        harness.setHand(player1, List.of(new SkyHussar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(whiteCreature.isTapped()).isFalse();
        assertThat(blueCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Forecast taps two controlled white or blue creatures and draws a card")
    void forecastTapsEligibleCreaturesAndDraws() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new SilkwingScout());
        Permanent ineligibleCreature = harness.addToBattlefieldAndReturn(player1, new GnatAlleyCreeper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        SkyHussar skyHussar = new SkyHussar();
        harness.setHand(player1, List.of(skyHussar));
        SkyHussar drawnCard = new SkyHussar();
        harness.setLibrary(player1, List.of(drawnCard));
        advanceToUpkeep(player1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(skyHussar);
        assertThat(whiteCreature.isTapped()).isTrue();
        assertThat(blueCreature.isTapped()).isTrue();
        assertThat(ineligibleCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(skyHussar, drawnCard);
    }

    @Test
    @DisplayName("Forecast cannot use a tapped eligible creature to pay its cost")
    void forecastRequiresTwoUntappedEligibleCreatures() {
        advanceToUpkeep(player1);
        Permanent tappedWhiteCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent untappedWhiteCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        tappedWhiteCreature.tap();
        harness.setHand(player1, List.of(new SkyHussar()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");

        assertThat(tappedWhiteCreature.isTapped()).isTrue();
        assertThat(untappedWhiteCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Forecast cannot be activated more than once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        harness.addToBattlefield(player1, new MistralCharger());
        harness.addToBattlefield(player1, new SilkwingScout());
        harness.setHand(player1, List.of(new SkyHussar()));
        advanceToUpkeep(player1);

        harness.activateHandAbility(player1, 0, null);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast requires its controller's upkeep and two eligible creatures")
    void forecastChecksTimingAndTapCost() {
        harness.addToBattlefield(player1, new MistralCharger());
        harness.addToBattlefield(player1, new GnatAlleyCreeper());
        harness.setHand(player1, List.of(new SkyHussar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");

        advanceToUpkeep(player1);
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
    }

    @Test
    @DisplayName("Forecast can tap two white creatures even with summoning sickness")
    void forecastAcceptsTwoWhiteSummoningSickCreatures() {
        advanceToUpkeep(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        SkyHussar source = new SkyHussar();
        SkyHussar drawn = new SkyHussar();
        harness.setHand(player1, List.of(source));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateHandAbility(player1, 0, null);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source, drawn);
    }

    @Test
    @DisplayName("Forecast cannot be activated during the opponent's upkeep")
    void forecastRejectsOpponentUpkeep() {
        advanceToUpkeep(player2);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SilkwingScout());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SilkwingScout());
        harness.setHand(player1, List.of(new SkyHussar()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Separate Sky Hussars can each forecast in the same upkeep using two blue creatures")
    void forecastLimitIsPerCard() {
        advanceToUpkeep(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SilkwingScout());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SilkwingScout());
        SkyHussar firstSource = new SkyHussar();
        SkyHussar secondSource = new SkyHussar();
        harness.setHand(player1, List.of(firstSource, secondSource));
        harness.setLibrary(player1, List.of(new MistralCharger(), new MistralCharger()));

        harness.activateHandAbility(player1, 0, null);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());
        first.untap();
        second.untap();
        harness.activateHandAbility(player1, 1, null);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(firstSource, secondSource);
    }

    @Test
    @DisplayName("Forecast keeps only its source revealed until the upkeep ends")
    void forecastKeepsSourceRevealedDuringUpkeep() throws Exception {
        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new MistralCharger());
        harness.addToBattlefield(player1, new SilkwingScout());
        SkyHussar source = new SkyHussar();
        harness.setHand(player1, List.of(source, new GnatAlleyCreeper()));
        harness.setLibrary(player1, List.of(new MistralCharger()));

        harness.activateHandAbility(player1, 0, null);
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

    @Test
    @DisplayName("The enter trigger untaps Sky Hussar itself and red creatures but leaves artifacts tapped")
    void enterTriggerIncludesSourceAndCreaturesOfAnyColor() {
        Permanent redCreature = harness.addToBattlefieldAndReturn(player1, new GnatAlleyCreeper());
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new AzoriusSignet());
        redCreature.tap();
        signet.tap();
        harness.setHand(player1, List.of(new SkyHussar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Sky Hussar");
        source.tap();
        assertThat(redCreature.isTapped()).isTrue();
        resolveAllTriggers();

        assertThat(source.isTapped()).isFalse();
        assertThat(redCreature.isTapped()).isFalse();
        assertThat(signet.isTapped()).isTrue();
    }
}
