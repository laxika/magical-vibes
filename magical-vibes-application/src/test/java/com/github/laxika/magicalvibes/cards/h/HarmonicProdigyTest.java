package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhituJourneymage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.cards.s.ShamanOfSpring;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmonicProdigy.class, GhituJourneymage.class, ShamanOfSpring.class,
        MentorOfTheMeek.class, GrizzlyBears.class, Shock.class, Forest.class})
class HarmonicProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess triggers once for a noncreature spell")
    void prowessTriggersOnce() {
        Permanent prodigy = addProdigy();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(4);
    }

    @Test
    @DisplayName("Another Wizard's triggered ability triggers twice")
    void doublesAnotherWizardTrigger() {
        addProdigy();

        harness.setHand(player1, List.of(new GhituJourneymage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A Shaman's triggered ability triggers twice")
    void doublesShamanTrigger() {
        addProdigy();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.setHand(player1, List.of(new ShamanOfSpring()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A non-Wizard, non-Shaman trigger is not doubled")
    void doesNotDoubleUnrelatedTrigger() {
        addProdigy();
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    private Permanent addProdigy() {
        return harness.addToBattlefieldAndReturn(player1, new HarmonicProdigy());
    }
}
