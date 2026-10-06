package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyEelSchool.class, Forest.class, GrizzlyBears.class, Disperse.class})
class SkyEelSchoolTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sky-Eel School puts creature spell on stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SkyEelSchool()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sky-Eel School");
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        harness.setHand(player1, List.of(new SkyEelSchool()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sky-Eel School");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB draws a card then prompts for discard")
    void etbDrawsThenPromptsForDiscard() {
        harness.setHand(player1, List.of(new SkyEelSchool()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Drew one card (Forest), now awaiting discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Completing discard results in net zero cards in hand (loot)")
    void completingDiscardResultsInLoot() {
        harness.setHand(player1, List.of(new SkyEelSchool()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Hand has 1 card (drew GrizzlyBears), discard it
        harness.handleCardChosen(player1, 0);

        // Hand should be empty (cast Sky-Eel School from hand, drew 1, discarded 1)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player can choose which card to discard")
    void canChooseWhichCardToDiscard() {
        harness.setHand(player1, List.of(new SkyEelSchool(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Hand has [Forest, GrizzlyBears] - discard Forest (index 0), keep Grizzly Bears
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Discarded card goes to graveyard")
    void discardedCardGoesToGraveyard() {
        harness.setHand(player1, List.of(new SkyEelSchool()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The newly drawn card can be discarded while keeping an older card")
    void canDiscardNewlyDrawnCard() {
        Forest olderCard = new Forest();
        SkyEelSchool drawnCard = new SkyEelSchool();
        harness.setHand(player1, List.of(new SkyEelSchool(), olderCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(olderCard, drawnCard);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(olderCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The enter trigger still draws and discards after its source leaves")
    void triggerResolvesAfterSourceLeaves() {
        SkyEelSchool school = new SkyEelSchool();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(school, new Disperse()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        var sourceId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.assertNotOnBattlefield(player1, "Sky-Eel School");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(school, drawnCard);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Sky-Eel School");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
