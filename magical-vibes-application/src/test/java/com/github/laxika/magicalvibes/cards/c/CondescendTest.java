package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Condescend.class, AuriokChampion.class})
class CondescendTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell and then offers its controller scry 2")
    void countersSpellAndScriesTwo() {
        AuriokChampion champion = new AuriokChampion();
        harness.setHand(player1, List.of(champion));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.setHand(player2, List.of(new Condescend()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, champion.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Auriok Champion");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Paying X keeps the spell on the stack and still allows scry 2")
    void payingXKeepsSpellAndScriesTwo() {
        AuriokChampion champion = new AuriokChampion();
        harness.setHand(player1, List.of(champion));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new Condescend()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, champion.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Auriok Champion");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Auriok Champion"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Uses the announced X value when checking whether the target can pay")
    void usesAnnouncedXValueForCounterPayment() {
        AuriokChampion champion = new AuriokChampion();
        harness.setHand(player1, List.of(champion));
        harness.addMana(player1, ManaColor.WHITE, 3); // {W}{W} + only one mana remains

        harness.setHand(player2, List.of(new Condescend()));
        harness.addMana(player2, ManaColor.BLUE, 3); // {U} + X=2

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, champion.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Auriok Champion");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Completing scry 2 reorders Condescend's controller's library")
    void completingScryReordersControllerLibrary() {
        AuriokChampion champion = new AuriokChampion();
        harness.setHand(player1, List.of(champion));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Condescend condescend = new Condescend();
        harness.setHand(player2, List.of(condescend));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        AuriokChampion libraryTop = new AuriokChampion();
        Condescend libraryBottom = new Condescend();
        AuriokChampion libraryNext = new AuriokChampion();
        harness.setLibrary(player2, List.of(libraryTop, libraryBottom, libraryNext));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, champion.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(libraryTop, libraryBottom);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryBottom, libraryNext, libraryTop);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Auriok Champion");
        harness.assertInGraveyard(player2, "Condescend");
    }
}
