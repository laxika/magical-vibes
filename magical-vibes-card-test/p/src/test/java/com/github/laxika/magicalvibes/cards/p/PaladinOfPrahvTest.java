package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

@CardUsed({PaladinOfPrahv.class, AzoriusFirstWing.class, AzoriusSignet.class})
class PaladinOfPrahvTest extends BaseCardTest {

    @Test
    @DisplayName("The battlefield ability gains life equal to damage dealt")
    void battlefieldAbilityGainsLifeFromDamage() {
        addCreatureReady(player1, new PaladinOfPrahv());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, java.util.List.of(0));
        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Forecast keeps the card in hand and gains life from the target creature's damage")
    void forecastWatchesTargetCreature() {
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());
        PaladinOfPrahv paladin = new PaladinOfPrahv();
        harness.setHand(player1, List.of(paladin));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(paladin);
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(paladin);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Forecast expires at the end of the turn")
    void forecastExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new PaladinOfPrahv()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Forecast can be activated only once each turn")
    void forecastIsLimitedToOncePerTurn() {
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new PaladinOfPrahv()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast cannot be activated outside your upkeep")
    void forecastRequiresYourUpkeep() {
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new PaladinOfPrahv()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your upkeep");
    }

    @Test
    @DisplayName("Forecast targets creatures only")
    void forecastRejectsNoncreatureTarget() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        harness.setHand(player1, List.of(new PaladinOfPrahv()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forecast keeps only its source revealed until the upkeep ends")
    void forecastKeepsSourceRevealedDuringUpkeep() throws Exception {
        Permanent target = addCreatureReady(player1, new AzoriusFirstWing());
        PaladinOfPrahv source = new PaladinOfPrahv();
        harness.setHand(player1, List.of(source, new AzoriusSignet()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

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

    @Test
    @DisplayName("Forecast and the target Paladin's own damage trigger both gain life")
    void forecastStacksWithBattlefieldAbility() {
        Permanent target = addCreatureReady(player1, new PaladinOfPrahv());
        harness.setHand(player1, List.of(new PaladinOfPrahv()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());
        declareAttackers(player1, List.of(0));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Damage to a blocker gains the full damage amount, including excess damage")
    void battlefieldAbilityGainsLifeFromDamageToCreature() {
        addCreatureReady(player1, new PaladinOfPrahv());
        addCreatureReady(player2, new AzoriusFirstWing());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Azorius First-Wing");
    }

    @Test
    @DisplayName("Each copy can forecast once and their delayed triggers stack")
    void separateCopiesCanForecastTheSameCreature() {
        Permanent target = addCreatureReady(player1, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new PaladinOfPrahv(), new PaladinOfPrahv()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.ensurePriority(player1);
        harness.activateHandAbility(player1, 1, target.getId());
        harness.withAutoStop(TurnStep.UPKEEP, () -> resolveAllTriggers());
        declareAttackers(player1, List.of(0));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Forecast cannot be activated during the opponent's upkeep")
    void forecastRejectsOpponentsUpkeep() {
        Permanent target = addCreatureReady(player2, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new PaladinOfPrahv()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your upkeep");
    }
}
