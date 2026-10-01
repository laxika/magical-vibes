package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurrentonShieldBearers.class, BurrentonBombardier.class})
class BurrentonShieldBearersTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger valid targets are creatures, not players")
    void attackTriggerTargetsCreatures() {
        addCreatureReady(player1, new BurrentonShieldBearers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurrentonBombardier());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Attack trigger gives target creature +0/+3 until end of turn")
    void attackTriggerBoostsTargetCreature() {
        addCreatureReady(player1, new BurrentonShieldBearers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurrentonBombardier());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent targetAfter = gqs.findPermanentById(gd, target.getId());
        assertThat(targetAfter.getPowerModifier()).isEqualTo(0);
        assertThat(targetAfter.getToughnessModifier()).isEqualTo(3);
        assertThat(targetAfter.getEffectivePower()).isEqualTo(2);
        assertThat(targetAfter.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can target the attacking creature itself")
    void canTargetSelf() {
        Permanent bearer = addCreatureReady(player1, new BurrentonShieldBearers());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bearer.getId());
        harness.passBothPriorities();

        assertThat(bearer.getToughnessModifier()).isEqualTo(3);
        assertThat(bearer.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new BurrentonShieldBearers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurrentonBombardier());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent targetAfter = gqs.findPermanentById(gd, target.getId());
        assertThat(targetAfter.getToughnessModifier()).isEqualTo(3);

        // Combat left a blocker-declaration interaction pending (player2 has an untapped creature);
        // clear it so priority can pass through the end step into cleanup.
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(targetAfter.getToughnessModifier()).isEqualTo(0);
        assertThat(targetAfter.getEffectiveToughness()).isEqualTo(2);
    }
}
