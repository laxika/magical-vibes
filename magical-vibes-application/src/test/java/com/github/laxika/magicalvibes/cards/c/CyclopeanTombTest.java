package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({CyclopeanTomb.class, Mountain.class, Swamp.class, Shatter.class})
class CyclopeanTombTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep activation puts a mire counter on a non-Swamp land")
    void activationMakesLandASwamp() {
        Permanent tomb = addTomb();
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        prepareUpkeepActivation();

        harness.activateAbility(player1, 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThat(tomb.isTapped()).isTrue();
        assertThat(mountain.getCounterCount(CounterType.MIRE)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("A Swamp is not a legal target")
    void cannotTargetSwamp() {
        addTomb();
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        prepareUpkeepActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    @Test
    @DisplayName("After the Tomb reaches the graveyard, each upkeep removes one remembered land's mire counters")
    void graveyardTriggerRemovesRememberedCounter() {
        Permanent tomb = addTomb();
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        prepareUpkeepActivation();
        harness.activateAbility(player1, 0, null, mountain.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, tomb.getId());
        resolveAllTriggers();

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.SWAMP);

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();

        assertThat(mountain.getCounterCount(CounterType.MIRE)).isZero();
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    private Permanent addTomb() {
        harness.setHand(player1, List.of(new CyclopeanTomb()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent tomb = findPermanent(player1, "Cyclopean Tomb");
        tomb.setSummoningSick(false);
        return tomb;
    }

    private void prepareUpkeepActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
