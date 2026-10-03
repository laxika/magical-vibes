package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CemeteryDesecrator.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class CemeteryDesecratorTest extends BaseCardTest {

    private static final String REMOVE_COUNTERS_MODE = "Remove X counters from target permanent";
    private static final String DEBUFF_MODE =
            "Target creature an opponent controls gets -X/-X until end of turn";

    @Test
    @DisplayName("Its ETB exiles another graveyard card before choosing the mode")
    void etbExilesAnotherCardBeforeModeChoice() {
        Card exiled = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(exiled));

        harness.enterBattlefieldAndReturn(player1, new CemeteryDesecrator());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(graveyardChoice).isNotNull();
        assertThat(graveyardChoice.validCardIds()).containsExactly(exiled.getId());

        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiled);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("The debuff mode uses the exiled card's mana value")
    void debuffModeUsesExiledManaValue() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        Card exiled = new GrizzlyBears();
        chooseModeAfterExiling(exiled, DEBUFF_MODE);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter mode removes counters equal to the exiled card's mana value")
    void counterModeRemovesManaValueCounters() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Card exiled = new HillGiant();
        chooseModeAfterExiling(exiled, REMOVE_COUNTERS_MODE);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        for (int i = 0; i < 4; i++) {
            harness.handleListChoice(player1, "+1/+1 counters");
        }

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A zero-mana-value exiled card removes no counters")
    void zeroManaValueRemovesNoCounters() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        chooseModeAfterExiling(new Forest(), REMOVE_COUNTERS_MODE);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The death trigger excludes Cemetery Desecrator itself from the graveyard choice")
    void deathTriggerExcludesSourceCard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CemeteryDesecrator());
        Permanent target = addCreatureReady(player2, new HillGiant());
        Card exiled = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(exiled));
        source.setMarkedDamage(4);

        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(graveyardChoice).isNotNull();
        assertThat(graveyardChoice.validCardIds()).containsExactly(exiled.getId());

        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, DEBUFF_MODE);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counter removal cannot stop early while counters remain")
    void counterRemovalIsMandatory() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.setCounterCount(CounterType.CHARGE, 3);
        chooseModeAfterExiling(new CemeteryDesecrator(), REMOVE_COUNTERS_MODE);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Done"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counter removal removes all available counters when there are fewer than X")
    void removesAllAvailableMixedCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.setCounterCount(CounterType.CHARGE, 1);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        chooseModeAfterExiling(new CemeteryDesecrator(), REMOVE_COUNTERS_MODE);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "charge counters");
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty graveyard produces no reflexive ability")
    void emptyGraveyardsProduceNoFollowUp() {
        harness.enterBattlefieldAndReturn(player1, new CemeteryDesecrator());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The exile choice can use the controller's graveyard")
    void exilesCardFromOwnGraveyard() {
        Card exiled = new Forest();
        harness.setGraveyard(player1, List.of(exiled));
        harness.enterBattlefieldAndReturn(player1, new CemeteryDesecrator());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, REMOVE_COUNTERS_MODE);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Cemetery Desecrator"));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The debuff rejects the controller's own creature")
    void debuffRequiresOpponentCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CemeteryDesecrator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CemeteryDesecrator());
        chooseModeAfterExiling(new Forest(), DEBUFF_MODE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The debuff expires at the end of the turn")
    void debuffExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        chooseModeAfterExiling(new GrizzlyBears(), DEBUFF_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("A death trigger cannot exile itself when it is the only graveyard card")
    void deathWithNoOtherGraveyardCardHasNoFollowUp() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CemeteryDesecrator());
        source.setMarkedDamage(4);
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cemetery Desecrator");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void chooseModeAfterExiling(Card exiled, String mode) {
        harness.setGraveyard(player2, List.of(exiled));
        harness.enterBattlefieldAndReturn(player1, new CemeteryDesecrator());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
