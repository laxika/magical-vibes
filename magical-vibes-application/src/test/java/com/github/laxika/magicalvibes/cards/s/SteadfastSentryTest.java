package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteadfastSentry.class, GrizzlyBears.class, Shock.class})
class SteadfastSentryTest extends BaseCardTest {

    @Test
    @DisplayName("When Steadfast Sentry dies, it puts a +1/+1 counter on a creature you control")
    void deathTriggerPutsCounterOnControlledCreature() {
        harness.addToBattlefield(player1, new SteadfastSentry());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID sentryId = harness.getPermanentId(player1, "Steadfast Sentry");
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, sentryId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The death trigger can target only a creature you control")
    void deathTriggerTargetsOnlyControlledCreatures() {
        harness.addToBattlefield(player1, new SteadfastSentry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID sentryId = harness.getPermanentId(player1, "Steadfast Sentry");
        harness.castInstant(player2, 0, sentryId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(harness.getPermanentId(player1, "Grizzly Bears"));
    }

    @Test
    @DisplayName("The death trigger is skipped when no creature you control remains")
    void deathTriggerSkipsWithoutControlledCreature() {
        harness.addToBattlefield(player1, new SteadfastSentry());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID sentryId = harness.getPermanentId(player1, "Steadfast Sentry");
        harness.castInstant(player2, 0, sentryId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Vigilance keeps Steadfast Sentry untapped when it attacks")
    void attackingDoesNotTapSentry() {
        Permanent sentry = addCreatureReady(player1, new SteadfastSentry());

        declareAttackers(List.of(0));

        assertThat(sentry.isTapped()).isFalse();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A death trigger whose target dies does not put a counter on another creature")
    void deathTriggerDoesNotRetargetWhenTargetDies() {
        harness.addToBattlefield(player1, new SteadfastSentry());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new SteadfastSentry());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Steadfast Sentry"));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
