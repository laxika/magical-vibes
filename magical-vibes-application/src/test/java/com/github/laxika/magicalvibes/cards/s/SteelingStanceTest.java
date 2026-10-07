package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelingStance.class, MistralCharger.class, StoicEphemera.class, AzoriusSignet.class})
class SteelingStanceTest extends BaseCardTest {

    @Test
    @DisplayName("The spell boosts creatures you control until end of turn")
    void spellBoostsOwnCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        harness.setHand(player1, List.of(new SteelingStance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(opposingCreature.getPowerModifier()).isEqualTo(0);
        assertThat(opposingCreature.getToughnessModifier()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(0);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Forecast boosts the target creature and keeps the card in hand")
    void forecastBoostsTargetAndKeepsSourceInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        SteelingStance stance = new SteelingStance();
        harness.setHand(player1, List.of(stance));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(stance);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Forecast can be activated only once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        harness.setHand(player1, List.of(new SteelingStance()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, firstTarget.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast requires the controller's upkeep")
    void forecastRequiresUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        SteelingStance stance = new SteelingStance();
        harness.setHand(player1, List.of(stance));
        advanceToUpkeep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(stance);
    }

    @Test
    @DisplayName("Forecast requires a creature target during its controller's upkeep")
    void forecastRequiresCreatureTarget() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        SteelingStance stance = new SteelingStance();
        harness.setHand(player1, List.of(stance));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(stance);
    }

    @Test
    @DisplayName("Forecast's boost lasts only until end of turn")
    void forecastBoostExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        harness.setHand(player1, List.of(new SteelingStance()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Forecast can be activated again during the next upkeep")
    void forecastActivationLimitResetsOnNextTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StoicEphemera());
        harness.setHand(player1, List.of(new SteelingStance()));
        harness.setHand(player2, List.of());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Forecast cannot be activated during an opponent's upkeep")
    void forecastRejectsOpponentsUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        SteelingStance stance = new SteelingStance();
        harness.setHand(player1, List.of(stance));
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(stance);
        assertThat(target.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Each copy may forecast once during the same upkeep")
    void separateCopiesCanForecastAndStackBoosts() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        SteelingStance first = new SteelingStance();
        SteelingStance second = new SteelingStance();
        harness.setHand(player1, List.of(first, second));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("The spell affects creatures present at resolution, not later arrivals")
    void spellBoostsOnlyCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new SteelingStance()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new MistralCharger());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new MistralCharger());

        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Forecast keeps only its source revealed in hand until upkeep ends")
    void forecastSourceRemainsPublicAfterResolutionUntilUpkeepEnds() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        harness.setHand(player1, List.of(new SteelingStance(), new AzoriusSignet()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, target.getId());
        harness.clearMessages();

        harness.passBothPriorities();

        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\":[{")
                        && message.contains("Steeling Stance"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"opponentHand\":[{")
                        && message.contains("Azorius Signet"));

        harness.clearMessages();
        harness.passUntil(player1, TurnStep.DRAW);

        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\":[]"));
    }
}
