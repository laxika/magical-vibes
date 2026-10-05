package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GhituJourneymage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NabanDeanOfIteration.class, GhituJourneymage.class, MentorOfTheMeek.class,
        FugitiveWizard.class, ElvishVisionary.class, Forest.class, GrizzlyBears.class,
        MerfolkTrickster.class})
class NabanDeanOfIterationTest extends BaseCardTest {

    // ===== Self-ETB doubling =====

    @Test
    @DisplayName("Naban doubles Wizard self-ETB — Ghitu Journeymage ETB triggers twice")
    void doublesWizardSelfEtb() {
        harness.addToBattlefield(player1, new NabanDeanOfIteration());

        harness.setHand(player1, List.of(new GhituJourneymage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Two ETB triggers on stack (doubled by Naban)
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Doubled Ghitu Journeymage deals 4 damage total to each opponent")
    void doubledGhituDealsFourDamage() {
        harness.addToBattlefield(player1, new NabanDeanOfIteration());

        harness.setHand(player1, List.of(new GhituJourneymage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve first ETB trigger
        harness.passBothPriorities(); // resolve second ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(16); // 20 - 2 - 2
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    // ===== Ally creature enters trigger doubling =====

    @Test
    @DisplayName("Naban doubles Mentor of the Meek's trigger when a Wizard enters")
    void doublesAllyTriggerForWizardEntry() {
        harness.addToBattlefield(player1, new NabanDeanOfIteration());
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        // Cast Fugitive Wizard (1/1 Human Wizard) — power 1 triggers Mentor
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Mentor's trigger should be on the stack twice (doubled by Naban)
        assertThat(gd.stack).hasSize(2);
    }

    // ===== Naban entering doubles Mentor trigger for itself =====

    @Test
    @DisplayName("Naban entering the battlefield doubles Mentor's trigger for itself")
    void nabanEntryDoublesMentorTrigger() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        // Cast Naban (2/1 Wizard) — power 2 triggers Mentor
        // When Naban enters, it's on the BF so its static ability applies
        harness.setHand(player1, List.of(new NabanDeanOfIteration()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Mentor's trigger should be on the stack twice (Naban sees itself entering)
        assertThat(gd.stack).hasSize(2);
    }

    // ===== Non-Wizard not doubled =====

    @Test
    @DisplayName("Non-Wizard self-ETB is not doubled")
    void doesNotDoubleNonWizardSelfEtb() {
        harness.addToBattlefield(player1, new NabanDeanOfIteration());

        // Cast Elvish Visionary (1/1 Elf — NOT a Wizard) — ETB draws a card
        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Only one ETB trigger (not doubled)
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve ETB trigger — draw 1 card

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Non-Wizard does not double Mentor trigger")
    void doesNotDoubleMentorForNonWizard() {
        harness.addToBattlefield(player1, new NabanDeanOfIteration());
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        // Cast Grizzly Bears (2/2 Bear — NOT a Wizard) — power 2 triggers Mentor
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Mentor triggers only once (not doubled)
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Without Naban, no doubling =====

    @Test
    @DisplayName("Without Naban, Ghitu Journeymage ETB triggers only once")
    void noDoublingWithoutNaban() {
        harness.addToBattlefield(player1, new FugitiveWizard());

        harness.setHand(player1, List.of(new GhituJourneymage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Only one ETB trigger
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(18); // 20 - 2
    }

    @Test
    @DisplayName("An opponent's Naban does not double your Wizard's ETB")
    void opponentsNabanDoesNotDoubleYourTrigger() {
        harness.addToBattlefield(player2, new NabanDeanOfIteration());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new GhituJourneymage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each doubled Mentor trigger offers its own payment choice")
    void doubledMentorTriggersHaveIndependentPayments() {
        harness.addToBattlefield(player1, new NabanDeanOfIteration());
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubled Merfolk Trickster triggers may target different creatures")
    void doubledTargetedEtbAllowsIndependentTargets() {
        harness.addToBattlefield(player1, new NabanDeanOfIteration());
        var firstCreature = harness.addToBattlefieldAndReturn(player2, new GhituJourneymage());
        var secondCreature = harness.addToBattlefieldAndReturn(player2, new GhituJourneymage());
        harness.setHand(player1, List.of(new MerfolkTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, firstCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, secondCreature.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
