package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Farewell;
import com.github.laxika.magicalvibes.cards.g.GallifreyFallsNoMore;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.PsychicPaper;
import com.github.laxika.magicalvibes.cards.s.SlipOutTheBack;
import com.github.laxika.magicalvibes.cards.s.SpikefieldCave;
import com.github.laxika.magicalvibes.cards.s.SpikefieldHazard;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWarDoctor.class, GrizzlyBears.class, SlipOutTheBack.class,
        SpikefieldHazard.class, SpikefieldCave.class, Farewell.class, Murder.class, PsychicPaper.class,
        GallifreyFallsNoMore.class})
class TheWarDoctorTest extends BaseCardTest {

    @Test
    void putsTimeCounterOnAnotherPermanentPhasingOut() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SlipOutTheBack()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void putsTimeCounterWhenACardIsExiled() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        target.setMarkedDamage(1);
        harness.setHand(player1, List.of(new SpikefieldHazard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void attacksAndDealsDamageEqualToItsTimeCounters() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        doctor.setCounterCount(CounterType.TIME, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    void simultaneousExileOfSeveralCardsAddsOnlyOneCounter() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        harness.setGraveyard(player1, List.of(new TheWarDoctor(), new TheWarDoctor()));
        harness.setGraveyard(player2, List.of(new TheWarDoctor()));
        harness.setHand(player1, List.of(new Farewell()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castModalSorceryWithModes(player1, 0, 1, 4, new int[]{3}, List.of(), null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.exiledCards).hasSize(3);
        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void creatureAndAttachedEquipmentPhasingOutAddsOnlyOneCounter() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        Permanent target = addCreatureReady(player2, new TheWarDoctor());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new PsychicPaper());
        equipment.setAttachedTo(target.getId());
        equipment.setChosenName("The War Doctor");
        equipment.setChosenSubtype(CardSubtype.DOCTOR);
        harness.setHand(player2, List.of(new SlipOutTheBack()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(target, equipment);
        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    void phasingOutTheDoctorItselfDoesNotAddATimeCounter() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        harness.setHand(player1, List.of(new SlipOutTheBack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, doctor.getId());
        resolveAllTriggers();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(doctor);
        assertThat(doctor.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    void attackCanDamageAPlayer() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        doctor.setCounterCount(CounterType.TIME, 3);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    void attackChecksTimeCountersWhenItResolves() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        doctor.setCounterCount(CounterType.TIME, 2);
        Permanent target = addCreatureReady(player2, new TheWarDoctor());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            doctor.setCounterCount(CounterType.TIME, 4);
            resolveAllTriggers();
        });

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void attackUsesLastKnownCountersAfterTheDoctorDies() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        doctor.setCounterCount(CounterType.TIME, 2);
        Permanent target = addCreatureReady(player2, new TheWarDoctor());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            doctor.setCounterCount(CounterType.TIME, 4);
            harness.castAndResolveInstant(player2, 0, doctor.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(doctor.getCard());
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void nonlethallyDamagedCreatureIsExiledIfDestroyedLaterThatTurn() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        doctor.setCounterCount(CounterType.TIME, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            resolveAllTriggers();
            assertThat(target.getMarkedDamage()).isEqualTo(1);
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
            harness.castAndResolveInstant(player1, 0, target.getId());
            resolveAllTriggers();
        });

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void zeroDamageDoesNotExileACreatureDestroyedLater() {
        addCreatureReady(player1, new TheWarDoctor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            resolveAllTriggers();
            assertThat(target.getMarkedDamage()).isZero();
            harness.castAndResolveInstant(player1, 0, target.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void doctorDoesNotTriggerWhenItPhasesOutTogetherWithAnotherCreature() {
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        harness.setHand(player1, List.of(new GallifreyFallsNoMore()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalInstant(player1, 0, 1, List.of(other.getId(), doctor.getId()));
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(other, doctor);
        assertThat(gd.stack).isEmpty();
        assertThat(doctor.getCounterCount(CounterType.TIME)).isZero();
    }
}
