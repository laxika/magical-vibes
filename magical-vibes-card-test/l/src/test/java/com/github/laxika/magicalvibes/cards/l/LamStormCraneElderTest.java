package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MonasteryMentor;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LamStormCraneElder.class, MonasteryMentor.class, GrizzlyBears.class, Shock.class})
class LamStormCraneElderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell conjures a Monastery Mentor")
    void noncreatureSpellConjuresMonasteryMentor() {
        harness.addToBattlefield(player1, new LamStormCraneElder());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Monastery Mentor")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not conjure a Monastery Mentor")
    void creatureSpellDoesNotConjureMonasteryMentor() {
        harness.addToBattlefield(player1, new LamStormCraneElder());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Monastery Mentor")).isZero();
    }
}
