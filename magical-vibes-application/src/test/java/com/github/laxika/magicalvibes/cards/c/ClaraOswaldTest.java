package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SarahJaneSmith;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheTenthDoctor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClaraOswald.class, TheTenthDoctor.class, SarahJaneSmith.class,
        Spellbook.class, GrizzlyBears.class})
class ClaraOswaldTest extends BaseCardTest {

    @Test
    void doublesTriggeredAbilitiesOfDoctorsYouControl() {
        harness.addToBattlefield(player1, new ClaraOswald());
        TheTenthDoctor doctor = new TheTenthDoctor();
        doctor.setSubtypes(List.of(CardSubtype.DOCTOR));
        addCreatureReady(player1, doctor);
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.passBothPriorities();

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
}
