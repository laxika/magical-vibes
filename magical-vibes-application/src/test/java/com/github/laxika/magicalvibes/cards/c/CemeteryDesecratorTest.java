package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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
    @DisplayName("The counter mode removes the chosen number of mixed counters")
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

    private void chooseModeAfterExiling(Card exiled, String mode) {
        harness.setGraveyard(player2, List.of(exiled));
        harness.enterBattlefieldAndReturn(player1, new CemeteryDesecrator());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
