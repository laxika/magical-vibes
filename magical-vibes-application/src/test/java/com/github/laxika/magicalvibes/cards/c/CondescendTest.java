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

    @Test
    @DisplayName("With X zero the target controller can pay zero and Condescend still scries")
    void zeroXCanBePaidWithoutMana() {
        AuriokChampion champion = new AuriokChampion();
        harness.setHand(player1, List.of(champion));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new Condescend()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLibrary(player2, List.of(new Condescend(), new AuriokChampion()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, champion.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(champion.getId()));
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).hasSize(2);
    }

    @Test
    @DisplayName("Declining an affordable payment counters the spell and still scries")
    void decliningPaymentCountersAndScries() {
        AuriokChampion champion = new AuriokChampion();
        harness.setHand(player1, List.of(champion));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setHand(player2, List.of(new Condescend()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        Condescend onlyCard = new Condescend();
        harness.setLibrary(player2, List.of(onlyCard));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, champion.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Auriok Champion");
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(onlyCard);
        harness.assertInGraveyard(player2, "Condescend");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent the spell from being countered")
    void emptyLibraryStillCountersSpell() {
        AuriokChampion champion = new AuriokChampion();
        harness.setHand(player1, List.of(champion));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new Condescend()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, champion.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Auriok Champion");
        harness.assertInGraveyard(player2, "Condescend");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Condescend does not scry when its only target has left the stack")
    void missingTargetPreventsScry() {
        AuriokChampion champion = new AuriokChampion();
        Condescend firstCounter = new Condescend();
        Condescend secondCounter = new Condescend();
        AuriokChampion top = new AuriokChampion();
        Condescend next = new Condescend();
        harness.setHand(player1, List.of(champion));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(firstCounter, secondCounter));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLibrary(player2, List.of(top, next));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, champion.getId());
        harness.castInstant(player2, 0, 1, champion.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Auriok Champion");
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top, next);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, next);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstCounter, secondCounter);
        assertThat(gd.stack).isEmpty();
    }
}
