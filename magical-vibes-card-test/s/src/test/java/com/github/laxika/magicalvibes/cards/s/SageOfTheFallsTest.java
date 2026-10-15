package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SageOfTheFalls.class, YouthfulKnight.class, RovingKeep.class, Xenograft.class})
class SageOfTheFallsTest extends BaseCardTest {

    @Test
    @DisplayName("Its own non-Human entry may draw and then discard")
    void ownEntryMayDrawAndDiscard() {
        harness.setLibrary(player1, List.of(new RovingKeep()));
        harness.setHand(player1, List.of(new SageOfTheFalls(), new YouthfulKnight()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the draw skips the discard")
    void decliningDrawSkipsDiscard() {
        harness.setLibrary(player1, List.of(new RovingKeep()));
        harness.setHand(player1, List.of(new SageOfTheFalls()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A non-Human creature entering under your control triggers it")
    void anotherNonHumanEntryTriggers() {
        harness.addToBattlefield(player1, new SageOfTheFalls());
        harness.setLibrary(player1, List.of(new RovingKeep()));
        harness.setHand(player1, List.of(new RovingKeep(), new YouthfulKnight()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Human or opponent creatures entering do not trigger it")
    void humanOrOpponentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new SageOfTheFalls());
        harness.setHand(player1, List.of(new YouthfulKnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new RovingKeep()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(Xenograft.class)
    @DisplayName("Its own entry still triggers when Xenograft makes it Human")
    void ownHumanEntryStillTriggers() {
        harness.addToBattlefieldAndReturn(player1, new Xenograft())
                .setChosenSubtype(CardSubtype.HUMAN);
        harness.setLibrary(player1, List.of(new RovingKeep()));
        harness.castFromHand(player1, new SageOfTheFalls(), "{4}{U}");

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed(Xenograft.class)
    @DisplayName("A different creature made Human by Xenograft does not trigger")
    void otherCreatureMadeHumanDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new Xenograft())
                .setChosenSubtype(CardSubtype.HUMAN);
        harness.addToBattlefield(player1, new SageOfTheFalls());
        harness.castFromHand(player1, new RovingKeep(), "{7}");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The drawn card must be discarded when the hand was empty")
    void drawnCardIsDiscardedFromInitiallyEmptyHand() {
        harness.setLibrary(player1, List.of(new RovingKeep()));
        harness.castFromHand(player1, new SageOfTheFalls(), "{4}{U}");
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}
