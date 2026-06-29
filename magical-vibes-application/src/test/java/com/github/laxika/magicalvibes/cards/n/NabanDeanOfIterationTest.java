package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GhituJourneymage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

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
}
