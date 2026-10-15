package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BloodbraidElf;
import com.github.laxika.magicalvibes.cards.c.ClockworkDroid;
import com.github.laxika.magicalvibes.cards.e.EverybodyLives;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FourKnocks;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFirstDoctor.class, BloodbraidElf.class, GrizzlyBears.class, Forest.class,
        Tardis.class, FourKnocks.class, ClockworkDroid.class, EverybodyLives.class,
        MindStone.class, ThroesOfChaos.class})
class TheFirstDoctorTest extends BaseCardTest {

    @Test
    void searchesForTardisInLibraryOrGraveyard() {
        Card libraryTardis = new Tardis();
        Card graveyardTardis = new Tardis();
        harness.setLibrary(player1, List.of(libraryTardis));
        harness.setGraveyard(player1, List.of(graveyardTardis));
        harness.castFromHand(player1, new TheFirstDoctor(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(libraryTardis.getId(), graveyardTardis.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardTardis.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(graveyardTardis);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryTardis);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardTardis);
    }

    @Test
    void putsCounterOnArtifactOrCreatureWhenCascadeSpellIsCast() {
        harness.addToBattlefield(player1, new TheFirstDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent illegalTarget = harness.addToBattlefieldAndReturn(player1, new FourKnocks());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new BloodbraidElf(), "{2}{R}{G}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, illegalTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    void doesNotTriggerForNonCascadeSpell() {
        harness.addToBattlefield(player1, new TheFirstDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void retrievesTardisFromLibraryAndLeavesOtherCardsThere() {
        Card tardis = new Tardis();
        Card other = new MindStone();
        harness.setLibrary(player1, List.of(other, tardis));
        harness.setGraveyard(player1, List.of(new ClockworkDroid()));

        harness.castFromHand(player1, new TheFirstDoctor(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(tardis.getId());
        harness.handleMultipleCardsChosen(player1, List.of(tardis.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tardis);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
    }

    @Test
    void canFailToFindTardisInLibrary() {
        Card tardis = new Tardis();
        harness.setLibrary(player1, List.of(tardis));
        harness.setGraveyard(player1, List.of());

        harness.castFromHand(player1, new TheFirstDoctor(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tardis);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cascadeSorceryCanPutCounterOnOpponentsNoncreatureArtifact() {
        harness.addToBattlefield(player1, new TheFirstDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new ThroesOfChaos(), "{3}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    void opponentsCascadeSpellDoesNotTriggerCounterAbility() {
        harness.addToBattlefield(player1, new TheFirstDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setLibrary(player2, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new ThroesOfChaos(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cascadeGrantedByTardisTriggersCounterAbility() {
        Permanent tardis = harness.addToBattlefieldAndReturn(player1, new Tardis());
        tardis.setSummoningSick(false);
        harness.addToBattlefield(player1, new TheFirstDoctor());
        Permanent crew = addCreatureReady(player1, new ClockworkDroid());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, null);
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
                harness.handlePermanentChosen(player1, crew.getId());
            }
            harness.passBothPriorities();
        });

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new EverybodyLives(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, tardis.getId());
        harness.passBothPriorities();

        assertThat(tardis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }
}
