package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonasteryMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell pumps Monastery Mentor and creates a prowess Monk")
    void noncreatureSpellPumpsAndCreatesMonk() {
        harness.addToBattlefield(player1, new MonasteryMentor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mentor = findPermanent(player1, "Monastery Mentor");
        Permanent monk = findPermanent(player1, "Monk");
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, monk, Keyword.PROWESS)).isTrue();
    }

    @Test
    @DisplayName("A creature spell does not trigger Monastery Mentor")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MonasteryMentor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Monk")).isZero();
        Permanent mentor = findPermanent(player1, "Monastery Mentor");
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Monk token's prowess triggers on a later noncreature spell")
    void monkTokenHasProwess() {
        harness.addToBattlefield(player1, new MonasteryMentor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent monk = findPermanent(player1, "Monk");
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
    }
}
