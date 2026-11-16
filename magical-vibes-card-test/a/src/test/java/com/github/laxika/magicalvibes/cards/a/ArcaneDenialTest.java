package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.cards.b.Banefire;
import com.github.laxika.magicalvibes.cards.k.KjeldoranHomeGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneDenial.class, KjeldoranHomeGuard.class, Banefire.class})
class ArcaneDenialTest extends BaseCardTest {

    /** player1 casts Kjeldoran Home Guard, player2 counters it with Arcane Denial. */
    private void counterHomeGuard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new KjeldoranHomeGuard(), new KjeldoranHomeGuard(), new KjeldoranHomeGuard()));
        harness.setLibrary(player2, List.of(new KjeldoranHomeGuard(), new KjeldoranHomeGuard(), new KjeldoranHomeGuard()));

        KjeldoranHomeGuard homeGuard = new KjeldoranHomeGuard();
        harness.setHand(player1, List.of(homeGuard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new ArcaneDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, homeGuard.getId());
    }

    @Test
    @DisplayName("Counters the spell and schedules both delayed draws — nobody draws immediately")
    void countersAndSchedulesDraws() {
        counterHomeGuard();

        harness.assertNotOnBattlefield(player1, "Kjeldoran Home Guard");
        harness.assertInGraveyard(player1, "Kjeldoran Home Guard");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        List<TemporaryGlobalTriggeredAbility> scheduled = gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP);
        assertThat(scheduled).hasSize(2);
        assertThat(scheduled).allMatch(a -> a.controllerId().equals(player2.getId()));
        // The countered spell's controller is the one who may draw up to two; the caster simply draws one.
        assertThat(scheduled).extracting(TemporaryGlobalTriggeredAbility::watchedPlayerId)
                .containsExactlyInAnyOrder(player1.getId(), null);
    }

    @Test
    @DisplayName("At the next upkeep the caster draws one and the countered spell's controller may draw two")
    void upkeepDraws() {
        counterHomeGuard();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        // The caster's draw is automatic; the countered spell's controller chooses.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
    }

    @Test
    @DisplayName("The countered spell's controller may decline to draw")
    void mayDeclineToDraw() {
        counterHomeGuard();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Still schedules both draws when the targeted Banefire cannot be countered")
    void schedulesDrawsForUncounterableSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new KjeldoranHomeGuard(), new KjeldoranHomeGuard(), new KjeldoranHomeGuard()));
        harness.setLibrary(player2, List.of(new KjeldoranHomeGuard(), new KjeldoranHomeGuard(), new KjeldoranHomeGuard()));

        Banefire banefire = new Banefire();
        harness.setHand(player1, List.of(banefire));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new ArcaneDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 5, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, banefire.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).hasSize(2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
    }

    @Test
    @DisplayName("The countered spell's controller may draw exactly one card")
    void mayDrawOneCard() {
        counterHomeGuard();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Both delayed abilities are controlled by Arcane Denial's controller")
    void casterControlsBothDelayedAbilities() {
        counterHomeGuard();
        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getControllerId()).isEqualTo(player2.getId()));
    }

    @Test
    @DisplayName("An additional upkeep in the current turn does not trigger the delayed draws")
    void waitsForNextTurnsUpkeep() {
        counterHomeGuard();
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).hasSize(2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
    }
}
