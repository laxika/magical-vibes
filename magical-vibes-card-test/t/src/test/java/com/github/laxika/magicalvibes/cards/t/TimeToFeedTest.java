package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.p.PharikasCure;
import com.github.laxika.magicalvibes.cards.v.VoyagingSatyr;
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

@CardUsed({TimeToFeed.class, GrizzlyBears.class, LlanowarElves.class,
        NessianCourser.class, VoyagingSatyr.class, PharikasCure.class})
class TimeToFeedTest extends BaseCardTest {

    @Test
    @DisplayName("The fight kills the opponent's creature and the delayed trigger gains 3 life")
    void fightKillsTargetAndGainsLife() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();

        harness.castAndResolveSorcery(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 23);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The delayed trigger does not gain life when the fought creature survives")
    void noLifeGainWhenTargetSurvives() {
        Permanent ownCreature = addCreatureReady(player1, new LlanowarElves());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();

        harness.castAndResolveSorcery(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The second target must be a creature you control")
    void secondTargetMustBeControlledCreature() {
        Permanent firstOpponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstOpponentCreature.getId(), secondOpponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstTargetMustBeAnOpponentsCreature() {
        Permanent first = addCreatureReady(player1, new VoyagingSatyr());
        Permanent second = addCreatureReady(player1, new NessianCourser());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void simultaneousFightDeathsStillGainLife() {
        Permanent own = addCreatureReady(player1, new NessianCourser());
        Permanent opponent = addCreatureReady(player2, new NessianCourser());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();

        harness.castAndResolveSorcery(player1, 0, List.of(opponent.getId(), own.getId()));
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Nessian Courser");
        harness.assertInGraveyard(player2, "Nessian Courser");
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    void deathLaterInTheTurnGainsLife() {
        Permanent own = addCreatureReady(player1, new VoyagingSatyr());
        Permanent opponent = addCreatureReady(player2, new VoyagingSatyr());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();
        harness.castAndResolveSorcery(player1, 0, List.of(opponent.getId(), own.getId()));
        harness.assertLife(player1, 20);

        harness.setHand(player1, List.of(new PharikasCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, opponent.getId());
        harness.assertLife(player1, 22);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Voyaging Satyr");
        harness.assertLife(player1, 25);
    }

    @Test
    void delayedTriggerExpiresAtEndOfTurn() {
        Permanent own = addCreatureReady(player1, new VoyagingSatyr());
        Permanent opponent = addCreatureReady(player2, new VoyagingSatyr());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();
        harness.castAndResolveSorcery(player1, 0, List.of(opponent.getId(), own.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new PharikasCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, opponent.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Voyaging Satyr");
        harness.assertLife(player1, 22);
    }

    @Test
    void losingOwnTargetPreventsFightButStillRegistersDelayedTrigger() {
        Permanent own = addCreatureReady(player1, new VoyagingSatyr());
        Permanent opponent = addCreatureReady(player2, new VoyagingSatyr());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();
        harness.castSorcery(player1, 0, List.of(opponent.getId(), own.getId()));

        harness.setHand(player2, List.of(new PharikasCure()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, own.getId());
        harness.passBothPriorities();
        assertThat(opponent.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);

        harness.setHand(player1, List.of(new PharikasCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, opponent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Voyaging Satyr");
        harness.assertInGraveyard(player2, "Voyaging Satyr");
        harness.assertLife(player1, 25);
    }

    @Test
    void opponentDyingBeforeResolutionPreventsFightAndLifeGain() {
        Permanent own = addCreatureReady(player1, new VoyagingSatyr());
        Permanent opponent = addCreatureReady(player2, new VoyagingSatyr());
        harness.setHand(player1, List.of(new TimeToFeed(), new PharikasCure()));
        addManaForTimeToFeed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, List.of(opponent.getId(), own.getId()));
        harness.castAndResolveInstant(player1, 0, opponent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Voyaging Satyr");
        harness.assertOnBattlefield(player1, "Voyaging Satyr");
        assertThat(own.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 22);
    }

    @Test
    void bothTargetsDyingBeforeResolutionPreventsAllEffects() {
        Permanent own = addCreatureReady(player1, new VoyagingSatyr());
        Permanent opponent = addCreatureReady(player2, new VoyagingSatyr());
        harness.setHand(player1, List.of(new TimeToFeed(), new PharikasCure()));
        addManaForTimeToFeed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, List.of(opponent.getId(), own.getId()));
        harness.castAndResolveInstant(player1, 0, opponent.getId());

        harness.setHand(player2, List.of(new PharikasCure()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, own.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Time to Feed");
        harness.assertInGraveyard(player1, "Voyaging Satyr");
        harness.assertInGraveyard(player2, "Voyaging Satyr");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        assertThat(gd.stack).isEmpty();
    }

    private void addManaForTimeToFeed() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
