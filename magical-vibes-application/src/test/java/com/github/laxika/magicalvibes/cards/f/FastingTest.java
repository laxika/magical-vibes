package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fasting.class, GrizzlyBears.class, HowlingMine.class})
class FastingTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep puts a hunger counter on Fasting")
    void upkeepAddsHungerCounter() {
        Permanent fasting = addFasting();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(fasting.getCounterCount(CounterType.HUNGER)).isEqualTo(1);
    }

    @Test
    @DisplayName("The fifth hunger counter destroys Fasting")
    void fifthHungerCounterDestroysFasting() {
        Permanent fasting = addFasting();
        fasting.setCounterCount(CounterType.HUNGER, 4);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fasting");
        harness.assertInGraveyard(player1, "Fasting");
    }

    @Test
    @DisplayName("Skipping the draw step gains 2 life and skips draw-step triggers")
    void skippingDrawStepGainsLifeAndSkipsDrawStepTriggers() {
        addFasting();
        harness.addToBattlefield(player1, new HowlingMine());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        beginDrawStep(true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Fasting");
    }

    @Test
    @DisplayName("Declining the draw-step replacement draws a card and destroys Fasting")
    void decliningDrawStepReplacementDrawsAndDestroysFasting() {
        addFasting();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        beginDrawStep(false);
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Fasting");
        harness.assertInGraveyard(player1, "Fasting");
    }

    private Permanent addFasting() {
        return harness.addToBattlefieldAndReturn(player1, new Fasting());
    }

    private void beginDrawStep(boolean skip) {
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, skip);
    }
}
