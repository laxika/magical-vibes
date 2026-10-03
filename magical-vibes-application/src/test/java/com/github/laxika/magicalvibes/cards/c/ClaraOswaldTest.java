package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AdiposeOffspring;
import com.github.laxika.magicalvibes.cards.s.SarahJaneSmith;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheTenthDoctor;
import com.github.laxika.magicalvibes.cards.t.TheNinthDoctor;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClaraOswald.class, TheTenthDoctor.class, SarahJaneSmith.class,
        Spellbook.class, GrizzlyBears.class, TheNinthDoctor.class, AdiposeOffspring.class})
class ClaraOswaldTest extends BaseCardTest {

    @Test
    void doublesTriggeredAbilitiesOfDoctorsYouControl() {
        harness.addToBattlefield(player1, new ClaraOswald());
        TheTenthDoctor doctor = new TheTenthDoctor();
        addCreatureReady(player1, doctor);
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card)
                .containsExactly(first, second);
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(first.getId(), 3)
                .containsEntry(second.getId(), 3);
    }

    @Test
    void doesNotDoubleTriggeredAbilitiesOfNonDoctors() {
        harness.addToBattlefield(player1, new ClaraOswald());
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isOne();
    }

    @Test
    void doesNotDoubleAnOpponentsDoctorTrigger() {
        harness.addToBattlefield(player2, new ClaraOswald());
        addCreatureReady(player1, new TheTenthDoctor());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(first);
        assertThat(gd.exiledCardTimeCounters).containsEntry(first.getId(), 3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void doublesDoctorTriggerWhenAnotherCreatureAttacks() {
        harness.addToBattlefield(player1, new ClaraOswald());
        harness.addToBattlefield(player1, new TheTenthDoctor());
        addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(first, second);
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(first.getId(), 3)
                .containsEntry(second.getId(), 3);
    }

    @Test
    void doublesDoctorUntapTrigger() {
        harness.addToBattlefield(player1, new ClaraOswald());
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheNinthDoctor());
        doctor.tap();
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);

        harness.performUntapStep(player1);
        harness.passUntil(TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(gd.additionalUpkeepsRemaining).isEqualTo(2);
    }

    @Test
    void doesNotDoubleANonDoctorEnterTheBattlefieldTrigger() {
        harness.addToBattlefield(player1, new ClaraOswald());

        harness.castFromHand(player1, new AdiposeOffspring(), "{3}{W}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Alien")).isOne();
    }
}
