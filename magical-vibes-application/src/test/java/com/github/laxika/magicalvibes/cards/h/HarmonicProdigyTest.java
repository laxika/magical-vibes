package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FlamekinHarbinger;
import com.github.laxika.magicalvibes.cards.g.GhituJourneymage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmonicProdigy.class, FlamekinHarbinger.class, GhituJourneymage.class,
        GrizzlyBears.class, MentorOfTheMeek.class, Shock.class})
class HarmonicProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles a Wizard's triggered ability")
    void doublesWizardTriggeredAbility() {
        harness.addToBattlefield(player1, new HarmonicProdigy());

        harness.setHand(player1, List.of(new GhituJourneymage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubles a Shaman's triggered ability")
    void doublesShamanTriggeredAbility() {
        harness.addToBattlefield(player1, new HarmonicProdigy());

        harness.setHand(player1, List.of(new FlamekinHarbinger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Does not double a triggered ability from a non-Wizard non-Shaman")
    void doesNotDoubleUnrelatedTriggeredAbility() {
        harness.addToBattlefield(player1, new HarmonicProdigy());
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not double its own Prowess ability")
    void doesNotDoubleItsOwnProwess() {
        Permanent prodigy = harness.addToBattlefieldAndReturn(player1, new HarmonicProdigy());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(4);
    }
}
