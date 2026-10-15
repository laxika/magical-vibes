package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhituJourneymage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
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
        MentorOfTheMeek.class, MaskwoodNexus.class, GrizzlyBears.class, Shock.class, Forest.class})
class HarmonicProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess triggers once for a noncreature spell")
    void prowessTriggersOnce() {
        Permanent prodigy = addProdigy();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
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
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

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

    @Test
    @DisplayName("One noncreature spell creates exactly one prowess trigger")
    void createsOnlyOneProwessTrigger() {
        addProdigy();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY))
                .hasSize(1);
    }

    @Test
    @DisplayName("Two Prodigies each double the other one's prowess")
    void twoProdigiesDoubleEachOthersProwess() {
        Permanent first = addProdigy();
        Permanent second = addProdigy();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(5);
        for (int i = 0; i < 5; i++) {
            harness.passBothPriorities();
        }
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A Prodigy that is also a Shaman doubles its own prowess")
    void shamanProdigyDoublesItsOwnProwess() {
        Permanent prodigy = addProdigy();
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(3);
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An opponent's Prodigy does not double your prowess")
    void opponentsProdigyDoesNotDoubleProwess() {
        Permanent prodigy = addProdigy();
        harness.addToBattlefield(player2, new HarmonicProdigy());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(4);
    }

    private Permanent addProdigy() {
        return harness.addToBattlefieldAndReturn(player1, new HarmonicProdigy());
    }
}
