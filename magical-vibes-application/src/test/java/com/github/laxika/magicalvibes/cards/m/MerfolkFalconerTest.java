package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GnarlidColony;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkFalconer.class, AcademyDrake.class, Forest.class, GrizzlyBears.class, GnarlidColony.class})
class MerfolkFalconerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a kicked spell triggers scry 2")
    void kickedSpellTriggersScryTwo() {
        harness.addToBattlefield(player1, new MerfolkFalconer());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        Card cardToBottom = scry.cards().getFirst();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isNotSameAs(cardToBottom);
        assertThat(gd.playerDecks.get(player1.getId()).get(gd.playerDecks.get(player1.getId()).size() - 1))
                .isSameAs(cardToBottom);
    }

    @Test
    @DisplayName("Casting a non-kicked spell does not trigger scry")
    void nonKickedSpellDoesNotTriggerScry() {
        harness.addToBattlefield(player1, new MerfolkFalconer());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("An opponent's kicked spell does not trigger Falconer")
    void opponentsKickedSpellDoesNotTriggerScry() {
        harness.addToBattlefield(player2, new MerfolkFalconer());
        harness.setHand(player1, List.of(new GnarlidColony()));
        Card topCard = new MerfolkFalconer();
        harness.setLibrary(player2, List.of(topCard, new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
        harness.assertOnBattlefield(player1, "Gnarlid Colony");
    }

    @Test
    @DisplayName("Two Falconers scry separately before the kicked spell resolves")
    void twoFalconersTriggerSeparateScries() {
        harness.addToBattlefield(player1, new MerfolkFalconer());
        harness.addToBattlefield(player1, new MerfolkFalconer());
        GnarlidColony spell = new GnarlidColony();
        harness.setHand(player1, List.of(spell));
        Card first = new MerfolkFalconer();
        Card second = new GnarlidColony();
        Card third = new MerfolkFalconer();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry firstScry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(firstScry).isNotNull();
        assertThat(firstScry.cards()).containsExactly(first, second);
        harness.assertNotOnBattlefield(player1, "Gnarlid Colony");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        harness.passBothPriorities();

        PendingInteraction.Scry secondScry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(secondScry).isNotNull();
        assertThat(secondScry.cards()).containsExactly(third, second);
        harness.assertNotOnBattlefield(player1, "Gnarlid Colony");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        harness.assertOnBattlefield(player1, "Gnarlid Colony");
    }

    @Test
    @DisplayName("Scry 2 with one card in the library can put that card on the bottom")
    void scryWithOneCardLibrary() {
        harness.addToBattlefield(player1, new MerfolkFalconer());
        harness.setHand(player1, List.of(new GnarlidColony()));
        Card onlyCard = new MerfolkFalconer();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        harness.assertOnBattlefield(player1, "Gnarlid Colony");
    }
}
